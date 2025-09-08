package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.EmptyCommandPath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePathImpl;
import net.strokkur.commands.internal.intermediate.paths.LiteralCommandPath;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

class MethodTransform implements PathTransform, ForwardingMessagerWrapper {

    private final CommandParser parser;
    private final MessagerWrapper messager;

    public MethodTransform(final CommandParser parser, final MessagerWrapper messager) {
        this.parser = parser;
        this.messager = messager;
    }

    @Override
    public void transform(final CommandPath<?> parent, final Element element) {
        debug("> MethodTransform: parsing {} for '{}'", element, parent.toStringNoChildren());
        final ExecutableElement method = (ExecutableElement) element;
        final LiteralCommandPath path = this.parser.getLiteralPath(element, Executes.class, Executes::value);
        final CommandPath<?> thisPath = path == null ? new EmptyCommandPath() : path;

        this.parser.populateRequirements(thisPath, element);
        parent.addChild(thisPath);

        ExecutorType type = ExecutorType.NONE;

        final List<? extends VariableElement> parameters = method.getParameters();
        final List<VariableElement> arguments = new ArrayList<>(parameters.size() - 1);

        for (int i = 1, parametersSize = parameters.size(); i < parametersSize; i++) {
            final VariableElement param = parameters.get(i);

            //noinspection ConstantValue
            if (i == 1 && param.getAnnotation(Executor.class) != null) {
                if (param.asType().toString().equals(Classes.PLAYER)) {
                    type = ExecutorType.PLAYER;
                    continue;
                } else if (param.asType().toString().equals(Classes.ENTITY)) {
                    type = ExecutorType.ENTITY;
                    continue;
                }
            }

            arguments.add(param);
        }

        final List<List<CommandArgument>> args = this.parser.parseArguments(arguments, (TypeElement) method.getEnclosingElement());
        if (args.isEmpty()) {
            final ExecutablePath out = new ExecutablePathImpl(List.of(), method);
            out.setAttribute(AttributeKey.EXECUTOR_TYPE, type);
            thisPath.addChild(out);
            debug("> MethodTransform: no arguments found. Current tree for thisPath: {}", thisPath);
            return;
        }

        for (final List<CommandArgument> argList : args) {
            final ExecutablePath out = new ExecutablePathImpl(argList, method);
            out.setAttribute(AttributeKey.EXECUTOR_TYPE, type);
            thisPath.addChild(out);
        }
        debug("> MethodTransform: found arguments! Current tree for thisPath: {}", thisPath);
    }

    @Override
    public boolean hardRequirement(final Element element) {
        return element.getKind() == ElementKind.METHOD;
    }

    @Override
    public boolean weakRequirement(final Element element) {
        //noinspection ConstantValue
        return element.getAnnotation(Executes.class) != null;
    }

    @Override
    public MessagerWrapper delegateMessager() {
        return this.messager;
    }
}
