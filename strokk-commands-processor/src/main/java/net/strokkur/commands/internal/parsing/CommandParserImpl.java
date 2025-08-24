/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Literal;
import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.RequiresOP;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgumentImpl;
import net.strokkur.commands.internal.exceptions.HandledConversionException;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePathImpl;
import net.strokkur.commands.internal.intermediate.paths.LiteralCommandPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPathImpl;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandParserImpl implements CommandParser, ForwardingMessagerWrapper {

    private final MessagerWrapper messager;
    private final BrigadierArgumentConverter converter;

    public CommandParserImpl(final MessagerWrapper messager, final BrigadierArgumentConverter converter) {
        this.messager = messager;
        this.converter = converter;
    }

    @Override
    public LiteralCommandPath parseElement(final TypeElement typeElement) {
        debug("Parsing " + typeElement.getQualifiedName());
        final String commandName = typeElement.getAnnotation(Command.class).value();
        final List<LiteralCommandArgument> literals = Arrays.stream(commandName.split(" "))
            .map(literal -> LiteralCommandArgument.literal(literal, typeElement))
            .toList();
        final LiteralCommandPath literalPath = new LiteralCommandPath(literals);

        final List<? extends Element> enclosedElements = typeElement.getEnclosedElements();

        final List<? extends CommandPath<?>> rootPaths;
        if (typeElement.getKind() == ElementKind.RECORD) {
            final List<VariableElement> recordComponents = new ArrayList<>(enclosedElements.size());
            for (final Element element : enclosedElements) {
                if (element.getKind() == ElementKind.RECORD_COMPONENT) {
                    recordComponents.add((VariableElement) element);
                }
            }

            rootPaths = getRecordPath(typeElement.asType(), recordComponents);
            for (final CommandPath<?> path : rootPaths) {
                literalPath.addChild(path);
            }
        } else {
            rootPaths = List.of(literalPath);
        }

        for (Element element : enclosedElements) {
            if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD) {
                final CommandPath<?> parsed = parseElement((TypeElement) element);
                for (final CommandPath<?> rootPath : rootPaths) {
                    rootPath.addChild(parsed);
                }
            } else if (element.getKind() == ElementKind.METHOD) {
                debug("Found executes method element: " + element.getSimpleName());
                final ExecutableElement executableElement = (ExecutableElement) element;
                final Executes executesAnnotation = executableElement.getAnnotation(Executes.class);
                if (executesAnnotation == null) {
                    continue;
                }

                final List<? extends CommandPath<?>> executeRoots;
                if (executesAnnotation.value().isBlank()) {
                    executeRoots = rootPaths;
                } else {
                    final List<LiteralCommandArgument> subcommandLiterals = new ArrayList<>();
                    for (final String subcommand : executesAnnotation.value().split(" ")) {
                        subcommandLiterals.add(LiteralCommandArgument.literal(subcommand, executableElement));
                    }

                    final LiteralCommandPath subcommandLiteralPath = new LiteralCommandPath(subcommandLiterals);
                    rootPaths.forEach(root -> root.addChild(subcommandLiteralPath));
                    executeRoots = List.of(subcommandLiteralPath);
                }

                for (final ExecutablePath path : getExecutablePath(executableElement)) {
                    debug("| Generated path:");
                    debug(path.toString(2));
                    for (final CommandPath<?> rootPath : executeRoots) {
                        rootPath.addChild(path);
                    }
                }

                populateRequirements(element, executeRoots);
            }
        }

        populateRequirements(typeElement, List.of(literalPath));
        return literalPath;
    }

    private List<ExecutablePath> getExecutablePath(final ExecutableElement executableElement) {
        ExecutorType type = ExecutorType.NONE;
        if (executableElement.getParameters().size() >= 2) {
            final Element potentialExecutor = executableElement.getParameters().get(1);
            if (potentialExecutor.getAnnotation(Executor.class) != null) {
                if (potentialExecutor.asType().toString().equals(Classes.PLAYER)) {
                    type = ExecutorType.PLAYER;
                } else if (potentialExecutor.asType().toString().equals(Classes.ENTITY)) {
                    type = ExecutorType.ENTITY;
                }
            }
        }

        final List<List<CommandArgument>> argsList = toArguments(executableElement.getParameters(), type == ExecutorType.NONE ? 1 : 2);
        if (argsList.isEmpty()) {
            return List.of(new ExecutablePathImpl(List.of(), executableElement));
        }

        final List<ExecutablePath> out = new ArrayList<>();
        for (final List<CommandArgument> args : argsList) {
            final ExecutablePath path = new ExecutablePathImpl(args, executableElement);
            path.setAttribute(AttributeKey.EXECUTOR_TYPE, type);
            out.add(path);
        }

        return out;
    }

    private List<RecordPath> getRecordPath(final TypeMirror recordTypeMirror, final List<? extends VariableElement> parameters) {
        final List<RecordPath> out = new ArrayList<>();
        for (final List<CommandArgument> args : toArguments(parameters, 0)) {
            out.add(new RecordPathImpl(args, recordTypeMirror));
        }
        return out;
    }

    private List<List<CommandArgument>> toArguments(final List<? extends VariableElement> parameters, int startIndex) {
        final List<List<CommandArgument>> arguments = new ArrayList<>();
        arguments.add(new ArrayList<>());

        for (int i = startIndex, parametersSize = parameters.size(); i < parametersSize; i++) {
            final VariableElement parameter = parameters.get(i);
            debug("| Parsing parameter: " + parameter.getSimpleName());

            final Literal literal = parameter.getAnnotation(Literal.class);
            if (literal != null) {
                final String[] declared = literal.value();
                if (declared.length == 0) {
                    arguments.forEach(argumentList -> argumentList.add(LiteralCommandArgument.literal(parameter.getSimpleName().toString(), parameter)));
                } else if (declared.length == 1) {
                    arguments.forEach(argumentList -> argumentList.add(LiteralCommandArgument.literal(declared[0], parameter)));
                } else {
                    // This is a worst-case scenario. All nested lists need to be duplicated as many times as there are literals, with each
                    // list being added a different literal.

                    final List<List<CommandArgument>> empty = new ArrayList<>();
                    for (final String lit : declared) {
                        for (final List<CommandArgument> argument : arguments) {
                            final List<CommandArgument> clone = new ArrayList<>(argument);
                            clone.add(LiteralCommandArgument.literal(lit, parameter));
                            empty.add(clone);
                        }
                    }

                    arguments.clear();
                    arguments.addAll(empty);
                }
                continue;
            }

            final BrigadierArgumentType argumentType;
            try {
                argumentType = converter.getAsArgumentType(parameter);
            } catch (HandledConversionException e) {
                debug("  | Due to an handled exception, the parameter parsing has beeen cancelled.");
                continue;
            }

            debug("  | Successfully found Brigadier type: {}", argumentType);
            final String name = parameter.getSimpleName().toString();
            for (final List<CommandArgument> argument : arguments) {
                argument.add(new RequiredCommandArgumentImpl(argumentType, name, parameter));
            }
        }

        return arguments;
    }

    private void populateRequirements(Element element, List<? extends CommandPath<?>> paths) {
        if (element.getAnnotation(RequiresOP.class) != null) {
            paths.forEach(path -> path.setAttribute(AttributeKey.REQUIRES_OP, true));
        }

        final Permission permission = element.getAnnotation(Permission.class);
        if (permission != null) {
            paths.forEach(path -> path.setAttribute(AttributeKey.PERMISSION, permission.value()));
        }
    }

    @Override
    public MessagerWrapper delegateMessager() {
        return messager;
    }
}
