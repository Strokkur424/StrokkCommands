package net.strokkur.commands.internal;

import net.strokkur.commands.annotations.SuggestionClass;
import net.strokkur.commands.annotations.SuggestionField;
import net.strokkur.commands.annotations.SuggestionMethod;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

interface ArgumentInformation {
    String getArgumentName();

    Element getElement();

    @Nullable
    SuggestionProvider getSuggestionProvider();

    void setSuggestionProvider(SuggestionProvider provider);

    @NullUnmarked
    default void updateSuggestionProvider(@NonNull Element classElement, @NonNull Element parameter) {
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
                StrokkCommandsPreprocessor.getMessenger().ifPresent(messager -> messager.printError("The parameter already has another suggestion provider declared!", parameter));
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
                StrokkCommandsPreprocessor.getMessenger().ifPresent(messager -> messager.printError("The parameter already has another suggestion provider declared!", parameter));
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
