package io.avaje.validation.core.adapters;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.avaje.validation.adapter.RegexFlag;
import io.avaje.validation.adapter.ValidationAdapter;
import io.avaje.validation.adapter.ValidationContext;
import io.avaje.validation.adapter.ValidationRequest;

public final class BasicAdapters {
  private BasicAdapters() {}

  public static final ValidationContext.AnnotationFactory FACTORY = (annotationType, context, attributes) ->
    switch (annotationType.getSimpleName()) {
        case "Email" -> new EmailAdapter(context.message2(attributes), attributes);
        case "Null" -> new NullAdapter(context.message2(attributes));
        case "NotNull", "NonNull" -> new NotNullAdapter(context.message2(attributes));
        case "AssertTrue" -> new AssertBooleanAdapter(context.message2(attributes), false);
        case "AssertFalse" -> new AssertBooleanAdapter(context.message2(attributes), true);
        case "NotBlank" -> new NotBlankAdapter(context.message2(attributes));
        case "NotEmpty" -> new NotEmptyAdapter(context.message2(attributes));
        case "Past" -> new FuturePastAdapter(context.message("Past", attributes), true, false);
        case "PastOrPresent" -> new FuturePastAdapter(context.message("PastOrPresent", attributes), true, true);
        case "Future" -> new FuturePastAdapter(context.message("Future", attributes), false, false);
        case "FutureOrPresent" -> new FuturePastAdapter(context.message("FutureOrPresent", attributes), false, true);
        case "Pattern" -> new PatternAdapter(context.message2("{avaje.Pattern.message}", attributes), attributes);
        case "Size" -> new SizeAdapter(context.message2(attributes), attributes);
        default -> null;
      };

  private static final class PatternAdapter implements ValidationAdapter<CharSequence> {

    private final ValidationContext.Message message;
    private final Predicate<String> pattern;

    @SuppressWarnings("unchecked")
    public PatternAdapter(ValidationContext.Message message, Map<String, Object> attributes) {
      this.message = message;
      int flags = 0;

      List<RegexFlag> flags1 = (List<RegexFlag>) attributes.get("flags");
      if (flags1 != null) {
        for (final var flag : flags1) {
          flags |= flag.getValue();
        }
      }
      this.pattern = Pattern.compile((String) attributes.get("regexp"), flags).asMatchPredicate().negate();
    }

    @Override
    public boolean validate(CharSequence value, ValidationRequest req, String propertyName) {
      if (value == null || pattern.test(value.toString())) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class SizeAdapter implements ValidationAdapter<Object> {

    private final ValidationContext.Message message;
    private final int min;
    private final int max;

    public SizeAdapter(ValidationContext.Message message, Map<String, Object> attributes) {
      this.message = message;
      this.min = (int) attributes.get("min");
      this.max = (int) attributes.get("max");
    }

    @Override
    public boolean validate(Object value, ValidationRequest req, String propertyName) {
      if (value == null) {
        return true;
      }

      if (value instanceof final CharSequence sequence) {
        final var len = sequence.length();
        if (len > max || len < min) {
          req.addViolation(message, propertyName);
          return false;
        }
      }

      if (value instanceof final Collection<?> col) {
        final var len = col.size();
        if (len > max || len < min) {
          req.addViolation(message, propertyName);
          return len > 0;
        }
      }

      if (value instanceof final Map<?, ?> map) {
        final var len = map.size();
        if (len > max || len < min) {
          req.addViolation(message, propertyName);
          return len > 0;
        }
      }

      if (value.getClass().isArray()) {

        final var len = Array.getLength(value);
        if (len > max || len < min) {
          req.addViolation(message, propertyName);
          return len > 0;
        }
      }

      return true;
    }
  }

  private static final class NotBlankAdapter implements ValidationAdapter<CharSequence> {

    private final ValidationContext.Message message;

    public NotBlankAdapter(ValidationContext.Message message) {
      this.message = message;
    }

    @Override
    public boolean validate(CharSequence cs, ValidationRequest req, String propertyName) {
      if (cs == null || isBlank(cs)) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }

    private static boolean isBlank(final CharSequence cs) {
      final int strLen = cs.length();
      if (strLen == 0) {
        return true;
      }
      for (int i = 0; i < strLen; i++) {
        if (!Character.isWhitespace(cs.charAt(i))) {
          return false;
        }
      }
      return true;
    }
  }

  private static final class NotEmptyAdapter implements ValidationAdapter<Object> {

    private final ValidationContext.Message message;

    public NotEmptyAdapter(ValidationContext.Message message) {
      this.message = message;
    }

    @Override
    public boolean validate(Object value, ValidationRequest req, String propertyName) {
      if (value == null
          || value instanceof final Collection<?> col && col.isEmpty()
          || value instanceof final Map<?, ?> map && map.isEmpty()) {
        req.addViolation(message, propertyName);
        return false;
      } else if (value instanceof final CharSequence sequence) {
        final var len = sequence.length();
        if (len == 0) {
          req.addViolation(message, propertyName);
          return false;
        }
      } else if (value.getClass().isArray()) {

        final var len = Array.getLength(value);
        if (len == 0) {
          req.addViolation(message, propertyName);
          return false;
        }
      }

      return true;
    }
  }

  // AssertFalse/AssertTrue
  private static final class AssertBooleanAdapter implements ValidationAdapter<Boolean> {

    private final ValidationContext.Message message;
    private final Boolean assertBool;

    public AssertBooleanAdapter(ValidationContext.Message message, Boolean assertBool) {
      this.message = message;
      this.assertBool = assertBool;
    }

    @Override
    public boolean validate(Boolean type, ValidationRequest req, String propertyName) {
      if (assertBool.equals(type)) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class NotNullAdapter implements ValidationAdapter<Object> {

    private final ValidationContext.Message message;

    public NotNullAdapter(ValidationContext.Message message) {
      this.message = message;
    }

    @Override
    public boolean validate(Object value, ValidationRequest req, String propertyName) {
      if (value == null) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class NullAdapter implements ValidationAdapter<Object> {

    private final ValidationContext.Message message;

    public NullAdapter(ValidationContext.Message message) {
      this.message = message;
    }

    @Override
    public boolean validate(Object value, ValidationRequest req, String propertyName) {
      if (value != null) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }
}
