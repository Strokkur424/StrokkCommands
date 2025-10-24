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
import java.util.NoSuchElementException;
import java.util.Optional;

public interface AnnotationsHolder extends SourceElement {

  <T extends Annotation> @Nullable T getAnnotation(Class<T> type);

  default boolean hasAnnotation(final Class<? extends Annotation> type) {
    return getAnnotationOptional(type).isPresent();
  }

  default <T extends Annotation> @Nullable SourceClass getAnnotationSourceClassField(Class<T> type, String fieldName) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This class (" + getClass().getSimpleName() + ") does not implement #getAnnotationSourceClassField");
  }

  default <T extends Annotation> Optional<T> getAnnotationOptional(final Class<T> type) {
    return Optional.ofNullable(getAnnotation(type));
  }

  default <T extends Annotation> T getAnnotationElseThrow(final Class<T> type) throws NoSuchElementException {
    return getAnnotationOptional(type).orElseThrow(() -> new NoSuchElementException("No annotation of type " + type.getSimpleName() + " is present."));
  }
}
