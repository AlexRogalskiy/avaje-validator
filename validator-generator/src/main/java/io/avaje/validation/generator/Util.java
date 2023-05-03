package io.avaje.validation.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

final class Util {
  private Util() {}

  static boolean isValid(Element e) {
    return ValidPojoPrism.isPresent(e)
        || JavaxValidPrism.isPresent(e)
        || JakartaValidPrism.isPresent(e);
  }

  /**
   * Return true if the element has a Nullable annotation.
   */
  public static boolean isNullable(Element p) {
    for (final AnnotationMirror mirror : p.getAnnotationMirrors()) {
      if ("Nullable".equalsIgnoreCase(shortName(mirror.getAnnotationType().toString()))) {
        return true;
      }
    }
    return false;
  }

  static boolean validImportType(String type) {
    return type.indexOf('.') > 0;
  }

  static String packageOf(String cls) {
    final int pos = cls.lastIndexOf('.');
    return (pos == -1) ? "" : cls.substring(0, pos);
  }

  static String shortName(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }

  static String shortType(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else if (fullType.startsWith("java")) {
      return fullType.substring(p + 1);
    } else {
      var result = "";
      var foundClass = false;
      for (final String part : fullType.split("\\.")) {
        if (foundClass || Character.isUpperCase(part.charAt(0))) {
          foundClass = true;
          result += (result.isEmpty() ? "" : ".") + part;
        }
      }
      return result;
    }
  }

  static String trimAnnotations(String type) {
    final int pos = type.indexOf("@");
    if (pos == -1) {
      return type;
    }
    return type.substring(0, pos) + type.substring(type.lastIndexOf(' ') + 1);
  }
  /** Return the common parent package. */
  static String commonParent(String currentTop, String aPackage) {
    if (aPackage == null) return currentTop;
    if (currentTop == null) return packageOf(aPackage);
    if (aPackage.startsWith(currentTop)) {
      return currentTop;
    }
    int next;
    do {
      next = currentTop.lastIndexOf('.');
      if (next > -1) {
        currentTop = currentTop.substring(0, next);
        if (aPackage.startsWith(currentTop)) {
          return currentTop;
        }
      }
    } while (next > -1);

    return currentTop;
  }

  static String initCap(String input) {
    if (input.length() < 2) {
      return input.toUpperCase();
    } else {
      return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
  }

  static String escapeQuotes(String input) {
    return input.replaceAll("^\"|\"$", "\\\\\"");
  }

  static String initLower(String name) {
    final StringBuilder sb = new StringBuilder(name.length());
    boolean toLower = true;
    for (final char ch : name.toCharArray()) {
      if (Character.isUpperCase(ch)) {
        if (toLower) {
          sb.append(Character.toLowerCase(ch));
        } else {
          sb.append(ch);
        }
      } else {
        sb.append(ch);
        toLower = false;
      }
    }
    return sb.toString();
  }

  /**
   * Return the base type given the JsonAdapter type. Remove the "jsonb" sub-package and the
   * "JsonAdapter" suffix.
   */
  static String baseTypeOfAdapter(String adapterFullName) {
    final int posLast = adapterFullName.lastIndexOf('.');
    final int posPrior = adapterFullName.lastIndexOf('.', posLast - 1);
    final int nameEnd = adapterFullName.length() - 11; // "JsonAdapter".length();
    if (posPrior == -1) {
      return adapterFullName.substring(posLast + 1, nameEnd);
    }

    final String className =
        adapterFullName.substring(0, posPrior) + adapterFullName.substring(posLast, nameEnd);
    final int $index = className.indexOf("$");
    if ($index != -1) {
      return className.substring(0, $index);
    }
    return className;
  }
}
