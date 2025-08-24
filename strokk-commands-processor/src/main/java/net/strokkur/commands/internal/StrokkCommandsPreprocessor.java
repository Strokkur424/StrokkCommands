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
package net.strokkur.commands.internal;

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Literal;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.exceptions.HandledConversionException;
import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.Requirement;
import net.strokkur.commands.internal.multiliterals.MultiLiteralsTree;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.strokkur.commands.internal.util.Classes.COMMAND_SENDER;
import static net.strokkur.commands.internal.util.Classes.ENTITY;
import static net.strokkur.commands.internal.util.Classes.PLAYER;

@NullMarked
public class StrokkCommandsPreprocessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Command.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_21;
    }

    @Override
    @NullUnmarked
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final MessagerWrapper messagerWrapper = MessagerWrapper.wrap(super.processingEnv.getMessager());
        final BrigadierArgumentConverter converter = new BrigadierArgumentConverter(messagerWrapper);

        for (Element element : roundEnv.getElementsAnnotatedWith(Command.class)) {
            messagerWrapper.info("Currently processing {}...", element);

            CommandInformation information = getCommandInformation(element);

            List<ExecutorInformation> executorInformation = new ArrayList<>();
            try {
                getExecutorsRecursively((TypeElement) element, messagerWrapper, converter, executorInformation);
            } catch (HandledConversionException e) {
                continue;
            }

            messagerWrapper.info("Here is the current tree:");
            for (ExecutorInformation info : executorInformation) {
                messagerWrapper.info("| {}", info);
            }

            CommandTree tree = new CommandTree(information.commandName(), element, Utils.getAnnotatedRequirements(element));
            executorInformation.forEach(info -> tree.insert(info, messagerWrapper));

            ClassFilePrinter printer = new ClassFilePrinter(element.toString(), information, tree, messagerWrapper);
            printer.print(super.processingEnv.getFiler());
        }

        return true;
    }

    @NullUnmarked
    private @NonNull CommandInformation getCommandInformation(@NonNull Element element) {
        Command command = element.getAnnotation(Command.class);

        Description description = element.getAnnotation(Description.class);
        Aliases aliases = element.getAnnotation(Aliases.class);

        return new CommandInformation(command.value(),
            description != null ? description.value() : null,
            aliases != null ? aliases.value() : null
        );
    }

    @NullUnmarked
    private void getExecutorsRecursively(@NonNull TypeElement classElement,
                                         @NonNull MessagerWrapper messager,
                                         @NonNull BrigadierArgumentConverter converter,
                                         @NonNull List<ExecutorInformation> outList) throws HandledConversionException {
        for (Element element : classElement.getEnclosedElements()) {
            if (element.getAnnotation(Command.class) != null) {
                try {
                    getExecutorsRecursively((TypeElement) element, messager, converter, outList);
                } catch (HandledConversionException e) {
                    // ignore it, since it is handled
                }
            }
        }

        Command command = classElement.getAnnotation(Command.class);
        if (command == null) {
            messager.errorElement("An unexpected internal exception has occurred: classElement is not annotated with @Command", classElement);
            throw new HandledConversionException();
        }

        List<String> literals = List.of(command.value().split(" "));
        List<RequiredArgumentInformation> arguments = new ArrayList<>();

        if (classElement.getKind() == ElementKind.RECORD) {
            for (Element element : classElement.getEnclosedElements()) {
                if (element.getKind() != ElementKind.RECORD_COMPONENT) {
                    continue;
                }

                arguments.add(new RequiredArgumentInformation(
                    element.toString(),
                    (VariableElement) element,
                    converter.getAsArgumentType((VariableElement) element, element.toString(), element.asType().toString())
                ));
            }
        }

        outList.addAll(getAllExecutorInformation(classElement, converter, messager));

        List<ArgumentInformation> commandArguments = new ArrayList<>();
        for (String literal : literals) {
            commandArguments.add(new LiteralArgumentInfoImpl(literal, classElement, literal, false));
        }
        commandArguments.addAll(arguments);

        ArrayList<ExecutorInformation> copy = new ArrayList<>(outList);
        outList.clear();
        outList.addAll(copy.stream()
            .map(info -> info.prepend(commandArguments))
            .toList());
    }

    @NullUnmarked
    private @NonNull List<ExecutorInformation> getAllExecutorInformation(@NonNull TypeElement classElement, BrigadierArgumentConverter converter, MessagerWrapper messager) {
        List<ExecutorInformation> out = new ArrayList<>();

        classElement.getEnclosedElements().stream()
            .filter(element -> element.getAnnotation(Executes.class) != null)
            .map(methodElement -> getExecutorInformation(classElement, (ExecutableElement) methodElement, converter, messager))
            .filter(Objects::nonNull)
            .forEach(out::addAll);

        return out;
    }

    @NullUnmarked
    private @Nullable List<ExecutorInformation> getExecutorInformation(TypeElement classElement, ExecutableElement methodElement, BrigadierArgumentConverter converter, MessagerWrapper messager) {
        List<? extends VariableElement> parameters = methodElement.getParameters();
        List<? extends TypeMirror> parameterTypes = ((ExecutableType) methodElement.asType()).getParameterTypes();
        List<String> parameterClassNames = parameterTypes.stream().map(TypeMirror::toString).toList();

        if (parameterClassNames.isEmpty()) {
            messager.errorElement("Method annotated with @Executes must at least declare a CommandSender parameter!", methodElement);
            return null;
        }

        if (!parameterClassNames.getFirst().equals(COMMAND_SENDER)) {
            messager.errorElement("The first parameter of a method annotated with @Executes must be of type CommandSender!", methodElement);
            return null;
        }

        ExecutorType executorType = ExecutorType.NONE;
        if (parameterTypes.size() >= 2 && parameters.get(1).getAnnotation(Executor.class) != null) {
            executorType = switch (parameterClassNames.get(1)) {
                case PLAYER -> ExecutorType.PLAYER;
                case ENTITY -> ExecutorType.ENTITY;
                default -> null;
            };
        }

        if (executorType == null) {
            messager.errorElement("The executor has to be either an org.bukkit.entity.Player or an org.bukkit.entity.Entity!", parameters.get(1));
            return null;
        }

        MultiLiteralsTree tree = MultiLiteralsTree.create();
        String initialLiteralsString = methodElement.getAnnotation(Executes.class).value();
        if (!initialLiteralsString.isBlank()) {
            for (String literals : initialLiteralsString.split(" ")) {
                tree.insert(new LiteralArgumentInfoImpl(literals, methodElement, literals, false));
            }
        }

        for (int i = executorType == ExecutorType.NONE ? 1 : 2; i < parameterTypes.size(); i++) {
            Literal literalAnnotation = parameters.get(i).getAnnotation(Literal.class);
            if (literalAnnotation != null) {
                tree.insert(new LiteralArgumentInfoImpl(parameters.get(i).toString(), parameters.get(i), ""), List.of(literalAnnotation.value()));
                continue;
            }

            BrigadierArgumentType asBrigadier;
            try {
                asBrigadier = converter.getAsArgumentType(parameters.get(i), parameters.get(i).toString(), parameterClassNames.get(i));
            } catch (HandledConversionException e) {
                return null;
            }

            RequiredArgumentInformation argument = new RequiredArgumentInformation(parameters.get(i).toString(), parameters.get(i), asBrigadier);
            argument.updateSuggestionProvider(classElement, parameters.get(i), messager);
            tree.insert(argument);
        }

        List<Requirement> requirements = Utils.getAnnotatedRequirements(methodElement);
        executorType.addRequirement(requirements);

        final ExecutorType finalExecutorType = executorType;
        return tree.flatten().stream()
            .map(arguments -> new ExecutorInformation(methodElement, finalExecutorType, arguments, requirements))
            .toList();
    }
}