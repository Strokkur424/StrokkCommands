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

import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public interface SourceMethod extends AnnotationsHolder {

  SourceType getReturnType();

  List<SourceParameter> getParameters();

  String getName();

  Set<Modifier> getModifiers();

  SourceClass getEnclosed();

  List<SourceTypeAnnotation> getTypeAnnotations();

  default String getTypeAnnotationsString() {
    final List<SourceTypeAnnotation> annotations = getTypeAnnotations();
    if (annotations.isEmpty()) {
      return "";
    }

    return "<" + String.join(", ", annotations.stream()
        .map(SourceTypeAnnotation::getDefinitionString)
        .toList()) + ">";
  }

  default boolean isConstructor() {
    return false;
  }

  default Set<String> getImports() {
    final Set<String> out = new TreeSet<>(getReturnType().getImports());
    for (final SourceParameter parameter : getParameters()) {
      out.addAll(parameter.getImports());
    }
    for (final SourceTypeAnnotation typeAnnotation : getTypeAnnotations()) {
      out.addAll(typeAnnotation.getImports());
    }
    return Collections.unmodifiableSet(out);
  }
}
