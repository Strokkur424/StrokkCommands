package net.strokkur.commands.internal;

import javax.lang.model.element.Element;
import java.util.Arrays;
import java.util.Objects;

record LiteralArgumentInformation(String argumentName, Element element, String[] literals, boolean addToMethod) implements ArgumentInformation {

    LiteralArgumentInformation(String argumentName, Element element, String[] literals) {
        this(argumentName, element, literals, true);
    }

    @Override
    public String toString() {
        return "LiteralArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", literals=" + Arrays.toString(literals) +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiteralArgumentInformation that = (LiteralArgumentInformation) o;
        return Objects.equals(argumentName(), that.argumentName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(argumentName());
    }
}
