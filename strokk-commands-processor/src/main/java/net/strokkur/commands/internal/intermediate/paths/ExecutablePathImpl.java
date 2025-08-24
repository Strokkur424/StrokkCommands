package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.List;

public class ExecutablePathImpl extends SimpleCommandPathImpl<CommandArgument> implements ExecutablePath {

    private final ExecutableElement executesMethod;

    public ExecutablePathImpl(final List<CommandArgument> arguments, final ExecutableElement executesMethod) {
        super(arguments);
        this.executesMethod = executesMethod;
    }

    @Override
    public Element getExecutesMethod() {
        return executesMethod;
    }

    @Override
    SimpleCommandPathImpl<CommandArgument> createLeftSplit(final List<CommandArgument> args) {
        return new ExecutablePathImpl(args, executesMethod);
    }
}
