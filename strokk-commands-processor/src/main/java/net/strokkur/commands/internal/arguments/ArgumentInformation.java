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
package net.strokkur.commands.internal.arguments;

import net.strokkur.commands.annotations.SuggestionClass;
import net.strokkur.commands.annotations.SuggestionField;
import net.strokkur.commands.annotations.SuggestionMethod;
import net.strokkur.commands.internal.intermediate.SuggestionProvider;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public interface ArgumentInformation {

    String getArgumentName();

    Element getElement();

    @Nullable
    SuggestionProvider getSuggestionProvider();

    void setSuggestionProvider(SuggestionProvider provider);

    @NullUnmarked
    default void updateSuggestionProvider(@NonNull Element classElement, @NonNull Element parameter, @NonNull MessagerWrapper messager) {
        SuggestionClass suggestionClass = parameter.getAnnotation(SuggestionClass.class);
        if (suggestionClass != null) {
            TypeMirror classMirror = Utils.getAnnotationMirror(parameter, SuggestionClass.class, "value");
            if (classMirror != null) {
                setSuggestionProvider(SuggestionProvider.ofClass(classMirror));
            }
        }

        SuggestionMethod suggestionMethod = parameter.getAnnotation(SuggestionMethod.class);
        if (suggestionMethod != null) {
            if (suggestionClass != null) {
                messager.errorElement("The parameter already has another suggestion provider declared!", parameter);
            } else {
                TypeMirror classMirror = Utils.getAnnotationMirror(parameter, SuggestionMethod.class, "base");
                String classNameToUse = classElement.toString();

                if (classMirror != null) {
                    classNameToUse = classMirror.toString();
                }

                setSuggestionProvider(
                    suggestionMethod.reference()
                        ? SuggestionProvider.ofMethodReference(suggestionMethod.method(), classNameToUse)
                        : SuggestionProvider.ofMethod(suggestionMethod.method(), classNameToUse)
                );
            }
        }

        SuggestionField suggestionField = parameter.getAnnotation(SuggestionField.class);
        if (suggestionField != null) {
            if (suggestionClass != null || suggestionMethod != null) {
                messager.errorElement("The parameter already has another suggestion provider declared!", parameter);
            } else {
                TypeMirror classMirror = Utils.getAnnotationMirror(parameter, SuggestionField.class, "base");
                if (classMirror != null) {
                    String classNameToUse = classElement.toString();
                    if (!classMirror.toString().equals(Class.class.getName())) {
                        classNameToUse = classMirror.toString();
                    }

                    setSuggestionProvider(SuggestionProvider.ofField(suggestionField.field(), classNameToUse));
                }
            }
        }
    }
}
