package net.strokkur.commands.objects;

import com.mojang.brigadier.arguments.ArgumentType;

public final class RequiredArgumentInformation implements ArgumentInformation {
    
    private final String argumentName;
    private final ArgumentType<?> type;

    public RequiredArgumentInformation(String argumentName, ArgumentType<?> type) {
        this.argumentName = argumentName;
        this.type = type;
    }

    @Override
    public String getArgumentName() {
        return argumentName;
    }

    public ArgumentType<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "RequiredArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", type=" + type +
               '}';
    }
}
