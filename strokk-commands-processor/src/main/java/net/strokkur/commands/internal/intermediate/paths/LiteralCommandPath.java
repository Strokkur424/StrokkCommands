package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.LiteralCommandArgument;

import java.util.List;

public class LiteralCommandPath extends SimpleCommandPathImpl<LiteralCommandArgument> {

    public LiteralCommandPath(final List<LiteralCommandArgument> arguments) {
        super(arguments);
    }

    @Override
    SimpleCommandPathImpl<LiteralCommandArgument> createLeftSplit(final List<LiteralCommandArgument> args) {
        return new LiteralCommandPath(args);
    }
}
