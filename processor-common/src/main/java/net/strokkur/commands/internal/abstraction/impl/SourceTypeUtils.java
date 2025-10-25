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
package net.strokkur.commands.internal.abstraction.impl;

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceType;
import net.strokkur.commands.internal.abstraction.VoidSourceType;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Optional;

final class SourceTypeUtils {

  public static SourceClass getSourceClassType(final ProcessingEnvironment environment, final DeclaredType declared) {
    if (declared.asElement().getKind() == ElementKind.RECORD) {
      return new SourceRecordImpl(environment, declared);
    }
    return new SourceClassImpl(environment, declared);
  }

  public static SourceType getSourceType(final ProcessingEnvironment environment, final TypeMirror type) {
    return switch (type.getKind()) {
      case BYTE, CHAR, BOOLEAN, INT, LONG, FLOAT, DOUBLE, SHORT -> new SourcePrimitiveImpl((PrimitiveType) type);
      case VOID -> new VoidSourceType();
      case ARRAY -> new SourceArrayImpl(getSourceType(environment, ((ArrayType) type).getComponentType()));
      case DECLARED -> getSourceClassType(environment, (DeclaredType) type);
      case TYPEVAR -> new SourceTypeVariableImpl(type.toString());
      default -> throw new UnsupportedOperationException("Unknown type: " + type);
    };
  }

  @Nullable
  public static TypeMirror getAnnotationMirror(final Element element, final Class<? extends Annotation> annotationClass, final String fieldName) {
    String annotationName = annotationClass.getName();

    Optional<? extends AnnotationMirror> out = element.getAnnotationMirrors().stream()
        .filter(mirror -> ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationName))
        .findFirst();

    return out.flatMap(mirror -> mirror.getElementValues().entrySet().stream()
            .filter(entry -> entry.getKey().getSimpleName().contentEquals(fieldName))
            .map(entry -> (TypeMirror) entry.getValue().getValue())
            .findFirst())
        .orElse(null);
  }

  private SourceTypeUtils() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
}
