/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.commands.internal.abstraction;

import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface AnnotationsHolder extends SourceElement {

  /// Gets an annotation directly present on this element.
  /// Does NOT check for inherited annotations via meta-annotations.
  /// @see #getAnnotationIncludingInherited(Class)
  <T extends Annotation> @Nullable T getAnnotation(Class<T> type);

  /// Returns all annotations directly present on this element as SourceClass instances.
  List<SourceClass> getAllAnnotations();

  /// Checks if an annotation is present, either directly or via inheritance (meta-annotation).
  ///
  /// For example, if you have:
  /// ```java
  /// @DefaultExecutes
  /// public @interface DefaultExecutesSpecial {}
  /// ```
  /// And a method annotated with `@DefaultExecutesSpecial`, calling
  /// `hasAnnotation(DefaultExecutes.class)` will return `true`.
  default boolean hasAnnotation(final Class<? extends Annotation> type) {
    return getAnnotationOptionalIncludingInherited(type).isPresent();
  }

  default <T extends Annotation> @Nullable SourceClass getAnnotationSourceClassField(Class<T> type, String fieldName) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This class (" + getClass().getSimpleName() + ") does not implement #getAnnotationSourceClassField");
  }

  /// Gets an annotation directly present on this element.
  /// Does NOT check for inherited annotations via meta-annotations.
  /// @see #getAnnotationOptionalIncludingInherited(Class)
  default <T extends Annotation> Optional<T> getAnnotationOptional(final Class<T> type) {
    return Optional.ofNullable(getAnnotation(type));
  }

  /// Gets an annotation, checking both direct annotations and inherited via meta-annotations.
  ///
  /// If the annotation is directly present, returns it.
  /// If not, checks if any annotation on this element is itself annotated with the target annotation,
  /// and returns that inherited annotation.
  ///
  /// For example, if you have:
  /// ```java
  /// @DefaultExecutes("path")
  /// public @interface DefaultExecutesSpecial {}
  /// ```
  /// And a method annotated with `@DefaultExecutesSpecial`, calling
  /// `getAnnotationOptionalIncludingInherited(DefaultExecutes.class)` will return the
  /// `@DefaultExecutes("path")` annotation from the `@DefaultExecutesSpecial` annotation type.
  default <T extends Annotation> Optional<T> getAnnotationOptionalIncludingInherited(final Class<T> type) {
    // First check for direct annotation
    final T direct = getAnnotation(type);
    if (direct != null) {
      return Optional.of(direct);
    }

    // Check meta-annotations (inherited annotations)
    for (final SourceClass annotationClass : getAllAnnotations()) {
      final T inherited = annotationClass.getAnnotation(type);
      if (inherited != null) {
        return Optional.of(inherited);
      }
    }

    return Optional.empty();
  }

  default <T extends Annotation> T getAnnotationElseThrow(final Class<T> type) throws NoSuchElementException {
    return getAnnotationOptionalIncludingInherited(type).orElseThrow(() -> new NoSuchElementException("No annotation of type " + type.getSimpleName() + " is present."));
  }

  /// Finds all annotations on this element that are themselves annotated with the given meta-annotation.
  ///
  /// For example, if you have:
  /// ```java
  /// @ExecutorWrapper
  /// public @interface TimingWrapper {}
  /// ```
  /// And a class annotated with `@TimingWrapper`, calling
  /// `getAnnotationsWithMetaAnnotation(ExecutorWrapper.class)` will return a list
  /// containing the `TimingWrapper` annotation class.
  ///
  /// @param metaAnnotationType The meta-annotation to look for
  /// @return List of annotation classes that have the specified meta-annotation
  default List<SourceClass> getAnnotationsWithMetaAnnotation(final Class<? extends Annotation> metaAnnotationType) {
    return getAllAnnotations().stream()
        .filter(annotationClass -> annotationClass.getAnnotation(metaAnnotationType) != null)
        .toList();
  }

  /// Gets the first annotation on this element that has the given meta-annotation.
  ///
  /// @param metaAnnotationType The meta-annotation to look for
  /// @return Optional containing the first matching annotation class, or empty if none found
  default Optional<SourceClass> getFirstAnnotationWithMetaAnnotation(final Class<? extends Annotation> metaAnnotationType) {
    return getAllAnnotations().stream()
        .filter(annotationClass -> annotationClass.getAnnotation(metaAnnotationType) != null)
        .findFirst();
  }
}
