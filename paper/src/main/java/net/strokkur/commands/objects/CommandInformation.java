package net.strokkur.commands.objects;

import org.jspecify.annotations.Nullable;

public record CommandInformation(
    String commandName,
    @Nullable String description,
    String @Nullable [] aliases
) {
}
