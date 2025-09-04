package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;

import java.util.Collections;
import java.util.List;

public class EmptyCommandPath extends SimpleCommandPathImpl<CommandArgument> {

    public EmptyCommandPath() {
        super(Collections.emptyList());
    }

    @Override
    SimpleCommandPathImpl<CommandArgument> createLeftSplit(final List<CommandArgument> args) {
        return new EmptyCommandPath();
    }
}
