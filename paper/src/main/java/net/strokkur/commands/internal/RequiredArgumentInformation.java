package net.strokkur.commands.internal;

import javax.lang.model.element.VariableElement;
import java.util.Objects;

record RequiredArgumentInformation(String argumentName, VariableElement element, BrigadierArgumentType type) implements ArgumentInformation {

    @Override
    public String toString() {
        return "RequiredArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", type=" + type +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequiredArgumentInformation that = (RequiredArgumentInformation) o;
        return Objects.equals(argumentName(), that.argumentName()) && Objects.equals(type(), that.type());
    }

    @Override
    public int hashCode() {
        return Objects.hash(argumentName(), type());
    }
}
