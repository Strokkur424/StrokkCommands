package net.strokkur.commands.internal;

record RequiredArgumentInformation(String argumentName, BrigadierArgumentType type) implements ArgumentInformation {

    @Override
    public String toString() {
        return "RequiredArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", type=" + type +
               '}';
    }
}
