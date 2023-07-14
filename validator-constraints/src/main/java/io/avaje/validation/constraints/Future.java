package io.avaje.validation.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated element must be an instant, date or time in the future.
 *
 * <p><i>Now</i> is defined by the {@link ClockProvider} attached to the {@link Validator} or {@link
 * ValidatorFactory}. The default {@code clockProvider} defines the current time according to the
 * virtual machine, applying the current default time zone if needed.
 *
 * <p>Supported types are:
 *
 * <ul>
 *   <li>{@code java.util.Date}
 *   <li>{@code java.util.Calendar}
 *   <li>{@code java.time.Instant}
 *   <li>{@code java.time.LocalDate}
 *   <li>{@code java.time.LocalDateTime}
 *   <li>{@code java.time.LocalTime}
 *   <li>{@code java.time.MonthDay}
 *   <li>{@code java.time.OffsetDateTime}
 *   <li>{@code java.time.OffsetTime}
 *   <li>{@code java.time.Year}
 *   <li>{@code java.time.YearMonth}
 *   <li>{@code java.time.ZonedDateTime}
 * </ul>
 *
 * <p>{@code null} elements are considered valid.
 */
@Constraint
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(Future.List.class)
public @interface Future {

  String message() default "{avaje.Future.message}";

  Class<?>[] groups() default {};

  /** Defines several {@code @Future} constraints on the same element. */
  @Target({METHOD, FIELD})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    Future[] value();
  }
}
