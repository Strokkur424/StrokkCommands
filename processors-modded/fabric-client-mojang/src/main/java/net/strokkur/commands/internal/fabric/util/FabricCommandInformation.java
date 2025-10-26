package net.strokkur.commands.internal.fabric.util;

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.util.CommandInformation;
import org.jspecify.annotations.Nullable;

public record FabricCommandInformation(
    @Nullable SourceConstructor constructor,
    SourceClass sourceClass,
    String[] aliases
) implements CommandInformation {
}
