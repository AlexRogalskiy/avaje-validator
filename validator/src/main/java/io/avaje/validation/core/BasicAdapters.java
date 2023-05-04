package io.avaje.validation.core;

import java.lang.reflect.Array;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.avaje.validation.adapter.RegexFlag;
import io.avaje.validation.adapter.ValidationAdapter;
import io.avaje.validation.adapter.ValidationContext;
import io.avaje.validation.adapter.ValidationRequest;

final class BasicAdapters {
  private BasicAdapters() {}

  static final ValidationContext.AnnotationFactory FACTORY =
      (annotationType, context, attributes) ->
          switch (annotationType.getSimpleName()) {
            case "NotNull" -> new NotNullAdapter(context.message("NotNull", attributes));
            case "AssertTrue" -> new AssertTrueAdapter(context.message("AssertTrue", attributes));
            case "AssertFalse" -> new AssertFalseAdapter(
                context.message("AssertFalse", attributes));
            case "NotBlank" -> new NotBlankAdapter(context.message("NotBlank", attributes));
            case "Past", "PastOrPresent" -> new PastAdapter(context.message("Past", attributes));
            case "Future", "FutureOrPresent" -> new FutureAdapter(
                context.message("Future", attributes));
            case "Pattern" -> new PatternAdapter(
                context.message("Pattern", attributes), attributes);
            case "Size" -> new SizeAdapter(context.message("Size", attributes), attributes);
            default -> null;
          };

  private static final class PatternAdapter implements ValidationAdapter<CharSequence> {

    private final String message;
    private final Predicate<String> pattern;

    public PatternAdapter(String message, Map<String, Object> attributes) {
      this.message = message;
      int flags = 0;

      for (final var flag : (List<RegexFlag>) attributes.get("flags")) {
        flags |= flag.getValue();
      }
      this.pattern =
          Pattern.compile((String) attributes.get("regexp"), flags).asMatchPredicate().negate();
    }

    @Override
    public boolean validate(CharSequence value, ValidationRequest req, String propertyName) {
      if (value == null || pattern.test(propertyName)) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class SizeAdapter implements ValidationAdapter<Object> {

    private final String message;
    private final int min;
    private final int max;

    public SizeAdapter(String message, Map<String, Object> attributes) {
      this.message = message;
      this.min = (int) attributes.get("min");
      this.max = (int) attributes.get("max");
    }

    @Override
    public boolean validate(Object value, ValidationRequest req, String propertyName) {
      if (value == null) {
        if (min != -1) {
          req.addViolation("CollectionNull", propertyName);
        }
        return false;
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

  private static final class FutureAdapter implements ValidationAdapter<Object> {

    private final String message;

    public FutureAdapter(String message) {
      this.message = message;
    }

    @Override
    public boolean validate(Object obj, ValidationRequest req, String propertyName) {

      if (obj == null) {
        req.addViolation(message, propertyName);
        return false;
      }
      if (obj instanceof final Date date) {
        if (date.before(Date.from(Instant.now()))) {
          req.addViolation(message, propertyName);
          return false;
        }
      } else if (obj instanceof final TemporalAccessor temporalAccessor
          && (temporalAccessor instanceof final Instant ins && ins.isBefore(Instant.now())
              || temporalAccessor instanceof final LocalDate ld && ld.isBefore(LocalDate.now())
              || temporalAccessor instanceof final LocalDateTime ldt
                  && ldt.isBefore(LocalDateTime.now())
              || temporalAccessor instanceof final LocalTime lt && lt.isBefore(LocalTime.now())
              || temporalAccessor instanceof final ZonedDateTime zdt
                  && zdt.isBefore(ZonedDateTime.now())
              || temporalAccessor instanceof final OffsetDateTime odt
                  && odt.isBefore(OffsetDateTime.now())
              || temporalAccessor instanceof final OffsetTime ot && ot.isBefore(OffsetTime.now())
              || temporalAccessor instanceof final Year y && y.isBefore(Year.now())
              || temporalAccessor instanceof final YearMonth ym && ym.isBefore(YearMonth.now()))) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class PastAdapter implements ValidationAdapter<Object> {

    private final String message;

    public PastAdapter(String message) {
      this.message = message;
    }

    @Override
    public boolean validate(Object obj, ValidationRequest req, String propertyName) {

      if (obj == null) {
        req.addViolation(message, propertyName);
        return false;
      }
      if (obj instanceof final Date date) {
        if (date.after(Date.from(Instant.now()))) {
          req.addViolation(message, propertyName);
          return false;
        }
      } else if (obj instanceof final TemporalAccessor temporalAccessor
          && (temporalAccessor instanceof final Instant ins && ins.isAfter(Instant.now())
              || temporalAccessor instanceof final LocalDate ld && ld.isAfter(LocalDate.now())
              || temporalAccessor instanceof final LocalDateTime ldt
                  && ldt.isAfter(LocalDateTime.now())
              || temporalAccessor instanceof final LocalTime lt && lt.isAfter(LocalTime.now())
              || temporalAccessor instanceof final ZonedDateTime zdt
                  && zdt.isAfter(ZonedDateTime.now())
              || temporalAccessor instanceof final OffsetDateTime odt
                  && odt.isAfter(OffsetDateTime.now())
              || temporalAccessor instanceof final OffsetTime ot && ot.isAfter(OffsetTime.now())
              || temporalAccessor instanceof final Year y && y.isAfter(Year.now())
              || temporalAccessor instanceof final YearMonth ym && ym.isAfter(YearMonth.now()))) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class NotBlankAdapter implements ValidationAdapter<String> {

    private final String message;

    public NotBlankAdapter(String message) {
      this.message = message;
    }

    @Override
    public boolean validate(String str, ValidationRequest req, String propertyName) {
      if (str == null || str.isBlank()) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class AssertTrueAdapter implements ValidationAdapter<Boolean> {

    private final String message;

    public AssertTrueAdapter(String message) {
      this.message = message;
    }

    @Override
    public boolean validate(Boolean type, ValidationRequest req, String propertyName) {
      if (Boolean.FALSE.equals(type)) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class AssertFalseAdapter implements ValidationAdapter<Boolean> {

    private final String message;

    public AssertFalseAdapter(String message) {
      this.message = message;
    }

    @Override
    public boolean validate(Boolean type, ValidationRequest req, String propertyName) {
      if (Boolean.TRUE.equals(type)) {
        req.addViolation(message, propertyName);
        return false;
      }
      return true;
    }
  }

  private static final class NotNullAdapter implements ValidationAdapter<Object> {

    private final String message;

    public NotNullAdapter(String message) {
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
}
