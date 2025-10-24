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
package net.strokkur.commands.internal.intermediate.suggestions;

import net.strokkur.commands.internal.abstraction.SourceClass;

public interface SuggestionProvider {

  static SuggestionProvider ofClass(final SourceClass implementingClass) {
    return new ClassSuggestionProvider(implementingClass);
  }

  static SuggestionProvider ofMethodReference(final SourceClass classElement, final String methodName) {
    return new MethodReferenceSuggestionProvider(classElement, methodName);
  }

  static SuggestionProvider ofMethod(final SourceClass classElement, final String methodName) {
    return new MethodSuggestionProvider(classElement, methodName);
  }

  static SuggestionProvider ofField(final SourceClass classElement, final String fieldName) {
    return new FieldSuggestionProvider(classElement, fieldName);
  }

  String getProvider();

  SourceClass getClassElement();
}
