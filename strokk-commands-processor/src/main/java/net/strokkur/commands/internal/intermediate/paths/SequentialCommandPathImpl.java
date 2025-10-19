package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;

import java.util.List;

public class SequentialCommandPathImpl extends SimpleCommandPathImpl<CommandArgument> implements SequentialCommandPath {
  public SequentialCommandPathImpl(final List<CommandArgument> arguments) {
    super(arguments);
  }

  @Override
  SimpleCommandPathImpl<CommandArgument> createLeftSplit(final List<CommandArgument> args) {
    return new SequentialCommandPathImpl(args);
  }
}
