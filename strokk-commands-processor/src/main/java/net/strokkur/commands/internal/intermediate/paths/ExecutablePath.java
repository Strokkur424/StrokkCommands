package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;

import javax.lang.model.element.Element;

public interface ExecutablePath extends CommandPath<CommandArgument> {
    Element getExecutesMethod();
}
