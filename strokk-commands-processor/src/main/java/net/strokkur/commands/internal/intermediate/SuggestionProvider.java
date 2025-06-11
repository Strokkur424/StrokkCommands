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
package net.strokkur.commands.internal.intermediate;

import javax.lang.model.type.TypeMirror;

@FunctionalInterface
public interface SuggestionProvider {
    static SuggestionProvider ofClass(TypeMirror implementingClass) {
        return () -> "new " + implementingClass + "()";
    }

    static SuggestionProvider ofMethodReference(String methodName, String baseClass) {
        return () -> baseClass + "::" + methodName;
    }

    static SuggestionProvider ofMethod(String methodName, String baseClass) {
        return () -> baseClass + "." + methodName + "()";
    }

    static SuggestionProvider ofField(String fieldName, String baseClass) {
        return () -> baseClass + "." + fieldName;
    }

    String get();
}