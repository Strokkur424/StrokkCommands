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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface SourceParameter extends SourceVariable {
  SourceMethod getEnclosed();

  static String combineJavaDocsParameterString(final List<String> hardcoded, final @Nullable SourceMethod method) {
    return combineJavaDocsParameterString(hardcoded, method, (m) -> true);
  }

  static String combineJavaDocsParameterString(final List<String> hardcoded, final @Nullable SourceMethod method, final Predicate<SourceParameter> filter) {
    if (method == null) {
      return String.join(", ", hardcoded);
    }
    return combineJavaDocsParameterString(hardcoded, method.getParameters().stream()
        .filter(filter)
        .toList());
  }

  static String combineJavaDocsParameterString(final List<String> hardcoded, final List<? extends SourceVariable> method) {
    final List<String> parameterTypes = new ArrayList<>(hardcoded);
    parameterTypes.addAll(method.stream()
        .map(SourceVariable::getType)
        .map(SourceType::getSourceName)
        .toList());
    return String.join(", ", parameterTypes);
  }

  static String combineMethodParameterString(final List<String> hardcoded, final @Nullable SourceMethod method) {
    return combineMethodParameterString(hardcoded, method, (m) -> true);
  }

  static String combineMethodParameterString(final List<String> hardcoded, final @Nullable SourceMethod method, final Predicate<SourceParameter> filter) {
    if (method == null) {
      return String.join(", ", hardcoded);
    }
    return combineMethodParameterString(hardcoded, method.getParameters().stream()
        .filter(filter)
        .toList());
  }

  static String combineMethodParameterString(final List<String> hardcoded, final List<? extends SourceVariable> method) {
    final List<String> parameterTypes = new ArrayList<>(hardcoded);
    parameterTypes.addAll(method.stream()
        .map(var -> "final " + var.getType().getSourceName() + " " + var.getName())
        .toList());
    return String.join(", ", parameterTypes);
  }
}
