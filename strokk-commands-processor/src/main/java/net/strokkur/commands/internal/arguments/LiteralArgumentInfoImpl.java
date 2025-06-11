package net.strokkur.commands.internal.arguments;

import net.strokkur.commands.internal.intermediate.SuggestionProvider;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import java.util.Objects;

public final class LiteralArgumentInfoImpl implements LiteralArgumentInfo {
    private final String argumentName;
    private final Element element;
    private final String literal;

    private boolean addToMethod;
    private @Nullable SuggestionProvider suggestionProvider;

    public LiteralArgumentInfoImpl(String argumentName, Element element, String literal, boolean addToMethod) {
        this.argumentName = argumentName;
        this.element = element;
        this.literal = literal;
        this.addToMethod = addToMethod;
    }

    public LiteralArgumentInfoImpl(String argumentName, Element element, String literal) {
        this(argumentName, element, literal, true);
    }

    @Override
    public String toString() {
        return "LiteralArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", element=" + element +
               ", literal=" + literal +
               ", addToMethod=" + addToMethod +
               '}';
    }

    @Override
    public LiteralArgumentInfo withLiteral(String literal) {
        return new LiteralArgumentInfoImpl(argumentName, element, literal, addToMethod);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiteralArgumentInfoImpl that = (LiteralArgumentInfoImpl) o;
        return Objects.deepEquals(getLiteral(), that.getLiteral()) && Objects.equals(getArgumentName(), that.getArgumentName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArgumentName(), getLiteral());
    }

    @Override
    public String getArgumentName() {
        return argumentName;
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String getLiteral() {
        return literal;
    }

    @Override
    public boolean addToMethod() {
        return addToMethod;
    }

    public void setAddToMethod(boolean addToMethod) {
        this.addToMethod = addToMethod;
    }

    @Override
    @Nullable
    public SuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
    }

    @Override
    public void setSuggestionProvider(@Nullable SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }
}
