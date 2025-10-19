package net.strokkur.commands.internal.intermediate.attributes;

import net.strokkur.commands.internal.arguments.CommandArgument;

import java.util.List;

public interface Parameterizable {
  List<CommandArgument> parameterArguments();
}
