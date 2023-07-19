package io.avaje.validation.inject.spi;

import static java.util.stream.Collectors.toMap;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Locale;

import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.aop.AspectProvider;
import io.avaje.inject.spi.GenericType;
import io.avaje.validation.ValidMethod;
import io.avaje.validation.Validator;
import io.avaje.validation.adapter.ValidationContext;
import io.avaje.validation.inject.aspect.AOPMethodValidator;
import io.avaje.validation.inject.aspect.MethodAdapterProvider;

/** Plugin for avaje inject that provides a default Jsonb instance. */
public final class DefaultValidatorProvider implements io.avaje.inject.spi.Plugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[] {Validator.class};
  }

  @Override
  public Class<?>[] providesAspects() {
    return new Class<?>[] {ValidMethod.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {

    validator(builder);

    paramAspect(builder);
  }

  private void validator(BeanScopeBuilder builder) {
    builder.provideDefault(
        null,
        Validator.class,
        () -> {
          final var props = builder.propertyPlugin();
          final var validator =
              Validator.builder().failFast(props.equalTo("validation.failFast", "true"));

          props
              .get("validation.resourcebundle.names")
              .map(s -> s.split(","))
              .ifPresent(validator::addResourceBundles);

          props
              .get("validation.locale.default")
              .map(Locale::forLanguageTag)
              .ifPresent(validator::setDefaultLocale);

          props.get("validation.locale.addedLocales").stream()
              .flatMap(s -> Arrays.stream(s.split(",")))
              .map(Locale::forLanguageTag)
              .forEach(validator::addLocales);

          props
              .get("validation.temporal.tolerance.value")
              .map(Long::valueOf)
              .ifPresent(
                  l -> {
                    final var unit =
                        props
                            .get("validation.temporal.tolerance.chronoUnit")
                            .map(ChronoUnit::valueOf)
                            .orElse(ChronoUnit.MILLIS);
                    validator.temporalTolerance(Duration.of(l, unit));
                  });
          return validator.build();
        });
  }

  private void paramAspect(BeanScopeBuilder builder) {
    builder.provideDefault(
        null,
        new GenericType<AspectProvider<ValidMethod>>() {}.type(),
        () -> {
          final var methodValidator = new AOPMethodValidator();

          builder.addPostConstruct(
              b -> {
                final var ctx = (ValidationContext) b.get(Validator.class);
                final var map =
                    b.list(MethodAdapterProvider.class).stream()
                        .collect(toMap(MethodAdapterProvider::provide, p -> p));
                methodValidator.post(ctx, map);
              });

          return methodValidator;
        });
  }
}
