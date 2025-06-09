package net.strokkur.commands.internal;

import java.util.Arrays;

record LiteralArgumentInformation(String argumentName, String[] literals) implements ArgumentInformation {

    @Override
    public String toString() {
        return "LiteralArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", literals=" + Arrays.toString(literals) +
               '}';
    }
}
