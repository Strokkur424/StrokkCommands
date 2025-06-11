package net.strokkur.commands.internal.abstraction;

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