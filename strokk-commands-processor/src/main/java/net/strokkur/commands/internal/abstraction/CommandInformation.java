package net.strokkur.commands.internal.abstraction;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public record CommandInformation(
    String commandName,
    @Nullable String description,
    String @Nullable [] aliases
) {
    @Override
    public String toString() {
        return "CommandInformation{" +
               "commandName='" + commandName + '\'' +
               ", description='" + description + '\'' +
               ", aliases=" + Arrays.toString(aliases) +
               '}';
    }
}
