package net.strokkur.commands.objects;

import java.util.Arrays;

public final class LiteralArgumentInformation implements ArgumentInformation {
    
    private final String argumentName;
    private final String[] literals;

    public LiteralArgumentInformation(String argumentName, String[] literals) {
        this.argumentName = argumentName;
        this.literals = literals;
    }

    @Override
    public String getArgumentName() {
        return argumentName;
    }

    public String[] getLiterals() {
        return literals;
    }

    @Override
    public String toString() {
        return "LiteralArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", literals=" + Arrays.toString(literals) +
               '}';
    }
}
