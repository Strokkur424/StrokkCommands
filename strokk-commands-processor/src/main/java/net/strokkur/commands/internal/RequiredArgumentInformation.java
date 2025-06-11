package net.strokkur.commands.internal;

import org.jspecify.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import java.util.Objects;

final class RequiredArgumentInformation implements ArgumentInformation {
    private final String getArgumentName;
    private final VariableElement getElement;
    private final BrigadierArgumentType type;

    private @Nullable SuggestionProvider suggestionProvider = null;

    RequiredArgumentInformation(String getArgumentName, VariableElement getElement, BrigadierArgumentType type) {
        this.getArgumentName = getArgumentName;
        this.getElement = getElement;
        this.type = type;
    }

    @Override
    public String toString() {
        return "RequiredArgumentInformation{" +
               "argumentName='" + getArgumentName + '\'' +
               ", type=" + type +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequiredArgumentInformation that = (RequiredArgumentInformation) o;
        return Objects.equals(getArgumentName(), that.getArgumentName()) && Objects.equals(type(), that.type());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArgumentName(), type());
    }

    @Override
    public String getArgumentName() {return getArgumentName;}

    @Override
    public VariableElement getElement() {return getElement;}

    public BrigadierArgumentType type() {return type;}

    @Override
    @Nullable
    public SuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
    }

    @Override
    public void setSuggestionProvider(SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }
}
