package net.strokkur.commands.internal;

import java.util.Arrays;

record LiteralArgumentInformation(String argumentName, String[] literals, boolean addToMethod) implements ArgumentInformation {

    LiteralArgumentInformation(String argumentName, String[] literals) {
        this(argumentName, literals, true);
    }

    @Override
    public String toString() {
        return "LiteralArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", literals=" + Arrays.toString(literals) +
               '}';
    }
}
