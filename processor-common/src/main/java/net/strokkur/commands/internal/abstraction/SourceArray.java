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
import java.util.Set;

public interface SourceArray extends SourceType, SourceElement {

  SourceType getArrayType();

  @Override
  default String getSourceName() {
    return getArrayType().getSourceName() + "[]";
  }

  @Override
  default String getName() {
    return getArrayType().getName() + "[]";
  }

  @Override
  default String getFullyQualifiedName() {
    return getArrayType().getFullyQualifiedName() + "[]";
  }

  @Override
  default String getPackageName() {
    return getArrayType().getPackageName();
  }

  @Override
  default boolean isArray() {
    return true;
  }

  @Override
  default Set<String> getImports() {
    return getArrayType().getImports();
  }

  @Override
  default Set<Modifier> getModifiers() {
    return Set.of();
  }
}
