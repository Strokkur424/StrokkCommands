package net.strokkur.commands.internal;

import javax.lang.model.element.Element;
import java.util.Objects;

record LiteralArgumentInfoImpl(String argumentName, Element element, String literal, boolean addToMethod) implements LiteralArgumentInfo {

    LiteralArgumentInfoImpl(String argumentName, Element element, String literal) {
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
        return Objects.deepEquals(literal(), that.literal()) && Objects.equals(argumentName(), that.argumentName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(argumentName(), literal());
    }
}
