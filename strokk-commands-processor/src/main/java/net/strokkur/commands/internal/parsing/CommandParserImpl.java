package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Literal;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgumentImpl;
import net.strokkur.commands.internal.exceptions.HandledConversionException;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePathImpl;
import net.strokkur.commands.internal.intermediate.paths.LiteralCommandPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPathImpl;
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
                final ExecutableElement executableElement = (ExecutableElement) element;
                for (final ExecutablePath path : getExecutablePath(executableElement)) {
                    for (final CommandPath<?> rootPath : rootPaths) {
                        rootPath.addChild(path);
                    }
                }
            }
        }

        return literalPath;
    }

    private List<ExecutablePath> getExecutablePath(final ExecutableElement executableElement) {
        final List<ExecutablePath> out = new ArrayList<>();
        for (final List<CommandArgument> args : toArguments(executableElement.getParameters())) {
            out.add(new ExecutablePathImpl(args, executableElement));
        }
        return out;
    }

    private List<RecordPath> getRecordPath(final TypeMirror recordTypeMirror, final List<? extends VariableElement> parameters) {
        final List<RecordPath> out = new ArrayList<>();
        for (final List<CommandArgument> args : toArguments(parameters)) {
            out.add(new RecordPathImpl(args, recordTypeMirror));
        }
        return out;
    }

    private List<List<CommandArgument>> toArguments(final List<? extends VariableElement> parameters) {
        final List<List<CommandArgument>> arguments = new ArrayList<>();

        for (final VariableElement parameter : parameters) {
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

            try {
                for (final List<CommandArgument> argument : arguments) {
                    argument.add(new RequiredCommandArgumentImpl(converter.getAsArgumentType(parameter), parameter.getSimpleName().toString(), parameter));
                }
            } catch (HandledConversionException e) {
                // handled
            }
        }

        return arguments;
    }

    @Override
    public MessagerWrapper delegateMessager() {
        return messager;
    }
}
