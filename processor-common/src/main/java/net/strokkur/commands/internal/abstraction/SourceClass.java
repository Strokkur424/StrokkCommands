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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

public interface SourceClass extends SourceType, AnnotationsHolder {

  boolean isTopLevel();

  SourceClass getOuterClass() throws NoSuchElementException;

  List<SourceTypeAnnotation> getTypeAnnotations();

  default List<SourceMethod> getNestedMethods() {
    return getNestedMethods(unused -> true);
  }

  default List<SourceField> getNestedFields() {
    return getNestedFields(unused -> true);
  }

  default List<SourceClass> getNestedClasses() {
    return getNestedClasses(unused -> true);
  }

  List<SourceMethod> getNestedMethods(final Predicate<SourceMethod> predicate);

  List<SourceField> getNestedFields(final Predicate<SourceField> predicate);

  List<SourceClass> getNestedClasses(final Predicate<SourceClass> predicate);

  @Override
  default boolean isArray() {
    return false;
  }

  default boolean isRecord() {
    return false;
  }

  @Override
  default Set<String> getImports() {
    return Collections.singleton(getFullyQualifiedName());
  }
}
