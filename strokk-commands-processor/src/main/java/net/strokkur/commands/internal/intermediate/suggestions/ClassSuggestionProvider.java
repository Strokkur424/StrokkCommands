package net.strokkur.commands.internal.intermediate.suggestions;

import net.strokkur.commands.internal.StrokkCommandsPreprocessor;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public record ClassSuggestionProvider(TypeMirror type) implements SuggestionProvider {

    @Override
    public String getProvider() {
        return "new " + Utils.getTypeName(StrokkCommandsPreprocessor.getTypes().asElement(type)) + "()";
    }

    @Override
    @Nullable
    public TypeElement getClassElement() {
        return (TypeElement) StrokkCommandsPreprocessor.getTypes().asElement(type);
    }
}
