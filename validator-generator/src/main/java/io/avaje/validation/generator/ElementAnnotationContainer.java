package io.avaje.validation.generator;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public record ElementAnnotationContainer(
    GenericType genericType,
    boolean hasValid,
    Map<GenericType, String> annotations,
    Map<GenericType, String> typeUse1,
    Map<GenericType, String> typeUse2,
    Map<GenericType, String> crossParam) {

  static ElementAnnotationContainer create(Element element, boolean classLevel) {
    final var hasValid = ValidPrism.isPresent(element);
    String rawType;
    Map<GenericType, String> typeUse1;
    Map<GenericType, String> typeUse2;
    final Map<GenericType, String> crossParam = new HashMap<>();
    if (element instanceof final ExecutableElement executableElement) {
      rawType = Util.trimAnnotations(executableElement.getReturnType().toString());
      final var typeUse = Util.typeUse(executableElement.getReturnType().toString(), true);
      typeUse1 =
          typeUse.get(0).stream()
              .collect(toMap(GenericType::parse, AnnotationUtil::annotationAttributeMap));
      typeUse2 =
          typeUse.get(1).stream()
              .collect(toMap(GenericType::parse, AnnotationUtil::annotationAttributeMap));

    } else {
      rawType = Util.trimAnnotations(element.asType().toString());
      final var typeUse = Util.typeUse(element.asType().toString(), true);
      typeUse1 =
          typeUse.get(0).stream()
              .filter(not(String::isBlank))
              .collect(toMap(GenericType::parse, AnnotationUtil::annotationAttributeMap));
      typeUse2 =
          typeUse.get(1).stream()
              .filter(not(String::isBlank))
              .collect(toMap(GenericType::parse, AnnotationUtil::annotationAttributeMap));
    }
    final var genericType = GenericType.parse(rawType);

    final var annotations =
        element.getAnnotationMirrors().stream()
            .filter(m -> !ValidPrism.isInstance(m))
            .filter(m -> !classLevel || hasMetaConstraintAnnotation(m))
            .map(
                a -> {
                  if (CrossParamConstraintPrism.isPresent(a.getAnnotationType().asElement())) {
                    crossParam.put(
                        GenericType.parse(a.getAnnotationType().toString()),
                        AnnotationUtil.annotationAttributeMap(a, element));
                    return null;
                  }
                  return a;
                })
            .filter(Objects::nonNull)
            .collect(
                toMap(
                    a -> GenericType.parse(a.getAnnotationType().toString()),
                    a -> AnnotationUtil.annotationAttributeMap(a, element)));

    return new ElementAnnotationContainer(
        genericType, hasValid, annotations, typeUse1, typeUse2, crossParam);
  }

  static boolean hasMetaConstraintAnnotation(AnnotationMirror m) {
    return hasMetaConstraintAnnotation(m.getAnnotationType().asElement());
  }

  static boolean hasMetaConstraintAnnotation(Element element) {

    return ConstraintPrism.isPresent(element);
  }

  // it seems we cannot directly retrieve mirrors from var elements, for varElements needs special
  // handling

  static ElementAnnotationContainer create(VariableElement varElement) {
    final var asString = varElement.asType().toString();

    final var noGeneric = AnnotationUtil.splitString(asString, "<")[0];

    final var annotations =
        noGeneric.transform(s -> Util.typeUse(s, false)).get(0).stream()
            .filter(not(String::isBlank))
            .filter(not(Constants.VALID_ANNOTATIONS::contains))
            .collect(toMap(GenericType::parse, AnnotationUtil::annotationAttributeMap));

    final var rawType = Util.trimAnnotations(asString);
    final var typeUse = Util.typeUse(asString, true);
    final var typeUse1 =
        typeUse.get(0).stream()
            .filter(not(String::isBlank))
            .collect(toMap(GenericType::parse, AnnotationUtil::annotationAttributeMap));
    final var typeUse2 =
        typeUse.get(1).stream()
            .filter(not(String::isBlank))
            .collect(toMap(GenericType::parse, AnnotationUtil::annotationAttributeMap));

    final boolean hasValid = Constants.VALID_ANNOTATIONS.stream().anyMatch(noGeneric::contains);

    return new ElementAnnotationContainer(
        GenericType.parse(rawType), hasValid, annotations, typeUse1, typeUse2, Map.of());
  }

  public void addImports(Set<String> importTypes) {
    genericType.addImports(importTypes);
    annotations.keySet().forEach(t -> t.addImports(importTypes));
    typeUse1.keySet().forEach(t -> t.addImports(importTypes));
    typeUse2.keySet().forEach(t -> t.addImports(importTypes));
  }

  boolean isEmpty() {
    return annotations.isEmpty() && typeUse1.isEmpty() && typeUse2.isEmpty();
  }
}
