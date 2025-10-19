package net.strokkur.commands.internal.intermediate.attributes;

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public record ExecutableImpl(ExecutorType executorType, ExecutableElement executesMethod, List<CommandArgument> parameterArguments) implements Executable {
}
