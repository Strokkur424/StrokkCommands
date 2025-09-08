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
package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.BuildConstants;
import net.strokkur.commands.internal.StrokkCommandsPreprocessor;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePath;
import net.strokkur.commands.internal.intermediate.paths.LiteralCommandPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPath;
import net.strokkur.commands.internal.intermediate.requirement.Requirement;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class CommandTreePrinter extends AbstractPrinter {

    private static final Set<String> STANDARD_IMPORTS = Set.of(
        Classes.COMMAND,
        Classes.LITERAL_COMMAND_NODE,
        Classes.COMMAND_SOURCE_STACK,
        Classes.COMMANDS,
        Classes.LIST,
        Classes.NULL_MARKED
    );

    private final Stack<ExecuteAccess<?>> executeAccessStack = new Stack<>();

    private final CommandPath<?> commandPath;
    private final CommandInformation commandInformation;
    private final Set<String> printedInstances = new TreeSet<>();

    public CommandTreePrinter(final int indent, final @Nullable Writer writer, final CommandPath<?> commandPath, final CommandInformation commandInformation) {
        super(indent, writer);
        this.commandPath = commandPath;
        this.commandInformation = commandInformation;
    }

    public String getPackageName() {
        return ((PackageElement) commandInformation.classElement().getEnclosingElement()).getQualifiedName().toString();
    }

    public String getBrigadierClassName() {
        return commandInformation.classElement().getSimpleName().toString() + "Brigadier";
    }

    private String getTypeVariableName(Element type) {
        if (commandInformation.classElement() == type) {
            return "instance";
        }

        final StringBuilder builder = new StringBuilder();
        final List<String> names = Utils.getNestedClassNames(type);

        for (int i = 0, size = names.size(); i < size; i++) {
            final String name = names.get(i);

            if (i == 0) {
                builder.append(Character.toLowerCase(name.charAt(0))).append(name.substring(1));
            } else {
                builder.append(name);
            }
        }
        return builder.toString();
    }

    @Override
    public void print() throws IOException {
        final Set<String> imports = getImports();

        final String packageName = getPackageName();
        final String brigadierClassName = getBrigadierClassName();

        final String description = commandInformation.description() == null ? "null" : '"' + commandInformation.description() + '"';
        final String aliases = commandInformation.aliases() == null ? "" : '"' + String.join("\", \"", List.of(commandInformation.aliases())) + '"';

        println("package {};", packageName);
        println();
        printImports(imports);
        println();

        printBlock("""
                /**
                 * A class holding the Brigadier source tree generated from
                 * {@link {}} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
                 *
                 * @author Strokkur24 - StrokkCommands
                 * @version {}
                 * @see #create() Creating the LiteralArgumentNode.
                 * @see #register(Commands) Registering the command.
                 */
                @NullMarked""",
            commandInformation.classElement().getSimpleName().toString(),
            BuildConstants.VERSION
        );

        println("public final class {} {", brigadierClassName);
        incrementIndent();

        println();
        printBlock("""
                /**
                 * Shortcut for registering the command node returned from
                 * {@link #create()}. This method uses the provided aliases
                 * and description from the original source file.
                 * <p>
                 * <h3>Registering the command</h3>
                 * <p>
                 * This method can safely be called either in your plugin bootstrapper's
                 * {@link io.papermc.paper.plugin.bootstrap.PluginBootstrap#bootstrap(io.papermc.paper.plugin.bootstrap.BootstrapContext)}, your main
                 * class' {@link org.bukkit.plugin.java.JavaPlugin#onLoad()} or {@link org.bukkit.plugin.java.JavaPlugin#onEnable()}
                 * method.
                 * <p>
                 * You need to call it inside of a lifecycle event. General information can be found on the
                 * <a href="https://docs.papermc.io/paper/dev/lifecycle/">PaperMC Lifecycle API docs page</a>.
                 * The general use case might look like this (example given inside the {@code onEnable} method):
                 * <p>
                 * <pre>{@code
                 * this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
                 *     final Commands commands = event.registrar();
                 *     {}.register(commands);
                 * }
                 * }</pre>
                 */
                public static void register(Commands commands) {
                    commands.register(create(), {}, List.of({}));
                }""",
            brigadierClassName,
            description,
            aliases
        );

        println();

        printBlock("""
                /**
                 * A method for creating a Brigadier command node which denotes the declared command
                 * in {@link {}}. You can either retrieve the unregistered node with this method
                 * or register it directly with {@link #register(Commands)}.
                 */
                public static LiteralCommandNode<CommandSourceStack> create() {""",
            commandInformation.classElement().getSimpleName().toString()
        );
        incrementIndent();

        if (printInstanceFields(commandPath)) {
            println();
        }

        printIndent();
        print("return ");
        printPath(commandPath);
        incrementIndent();
        println(".build();");
        decrementIndent();
        decrementIndent();
        println("}");
        println();

        printBlock("""
                /**
                 * The constructor is not accessible. There is no need for an instance
                 * to be created, as no state is stored, and all methods are static.
                 *
                 * @throws IllegalAccessException
                 */
                private {}() throws IllegalAccessException {
                    throw new IllegalAccessException("Cannot create instance of static class.");
                }
                """,
            brigadierClassName);

        decrementIndent();
        println("}");
    }

    private void printImports(Set<String> imports) throws IOException {
        Map<Boolean, List<String>> splitImports = imports.stream()
            .sorted()
            .collect(Collectors.partitioningBy(str -> str.startsWith("java")));

        List<String> javaImports = splitImports.get(true);
        List<String> otherImports = splitImports.get(false);

        for (String i : otherImports) {
            println("import {};", i);
        }

        println();

        for (String i : javaImports) {
            println("import {};", i);
        }
    }

    private Set<String> getImports() {
        final Set<String> imports = new HashSet<>(STANDARD_IMPORTS);
        gatherImports(imports, commandPath);

        imports.removeIf(importString -> {
            if (importString.startsWith("java.lang")) {
                return true;
            }

            final TypeElement element = StrokkCommandsPreprocessor.getElements().getTypeElement(importString);
            if (element == null) {
                return false;
            }

            return Utils.getPackageElement(element) == Utils.getPackageElement(commandInformation.classElement());
        });

        return imports;
    }

    private void gatherImports(Set<String> imports, CommandPath<?> commandPath) {
        if (!(commandPath instanceof LiteralCommandPath)) {
            for (final CommandArgument arg : commandPath.getArguments()) {
                if (arg instanceof RequiredCommandArgument requiredArgument) {
                    imports.addAll(requiredArgument.getArgumentType().imports());

                    if (requiredArgument.getSuggestionProvider() != null) {
                        final TypeElement suggestionsTypeElement = requiredArgument.getSuggestionProvider().getClassElement();
                        if (suggestionsTypeElement != null) {
                            imports.add(suggestionsTypeElement.getQualifiedName().toString());
                        }
                    }
                }
            }
        }

        final ExecutorType executorType = commandPath.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);
        if (executorType == ExecutorType.PLAYER) {
            imports.add(Classes.PLAYER);
        } else if (executorType == ExecutorType.ENTITY) {
            imports.add(Classes.ENTITY);
        }

        if (executorType != ExecutorType.NONE) {
            imports.add(Classes.SIMPLE_COMMAND_EXCEPTION_TYPE);
            imports.add(Classes.MESSAGE_COMPONENT_SERIALIZER);
            imports.add(Classes.COMPONENT);
        }

        for (final CommandPath<?> child : commandPath.getChildren()) {
            gatherImports(imports, child);
        }
    }



    private boolean printInstanceFields(CommandPath<?> path) throws IOException {
        if (path.hasAttribute(AttributeKey.ACCESS_STACK)) {
            executeAccessStack.push(path.getAttributeNotNull(AttributeKey.ACCESS_STACK));

            // print the instance
            final String instanceName = Utils.getInstanceName(executeAccessStack);

            final ExecuteAccess<?> access = executeAccessStack.peek();
            final String typeName = Utils.getTypeName(access.getElement().asType());

            final String initializer;
            if (access instanceof FieldAccess fieldAccess && Utils.isFieldInitialized(fieldAccess.getElement())) {
                initializer = null;
            } else {
                initializer = "new %s()".formatted(typeName);
            }

            if (access instanceof FieldAccess fieldAccess && initializer == null)  {
                println("final {} {} = {}.{};",
                    typeName,
                    instanceName,
                    Utils.getInstanceName(executeAccessStack.subList(0, executeAccessStack.size() - 1)),
                    fieldAccess.getElement().getSimpleName()
                );
            } else {
                println("final {} {} = new {}();",
                    typeName,
                    instanceName,
                    typeName
                );
            }

            printedInstances.add(instanceName);
        }

        for (final CommandPath<?> child : path.getChildren()) {
            printInstanceFields(child);
        }

        if (path.hasAttribute(AttributeKey.ACCESS_STACK)) {
            executeAccessStack.pop();
        }

//
//        if (typeElement.getKind() == ElementKind.RECORD) {
//            return false;
//        }
//
//        final String varType = Utils.getTypeName(typeElement);
//        final String varName = getTypeVariableName(typeElement);
//
//        final String constructor;
//        if (typeElement.getModifiers().contains(Modifier.STATIC) || !typeElement.getNestingKind().isNested()) {
//            constructor = "new " + varType;
//        } else {
//            constructor = getTypeVariableName(typeElement.getEnclosingElement()) + ".new " + typeElement.getSimpleName();
//        }
//
//        println("final {} {} = {}();",
//            varType,
//            varName,
//            constructor
//        );
//
//        for (final Element element : typeElement.getEnclosedElements()) {
//            if (element instanceof TypeElement type && type.getKind() != ElementKind.RECORD) {
//                printInstanceFields(type);
//            }
//        }

        return true;
    }

    //<editor-fold name="Command Tree Printing Methods">
    private void printPath(CommandPath<?> path) throws IOException {
        if (path.hasAttribute(AttributeKey.ACCESS_STACK)) {
            executeAccessStack.push(path.getAttributeNotNull(AttributeKey.ACCESS_STACK));
        }

        if (path instanceof ExecutablePath executablePath && !executablePath.getAttributeNotNull(AttributeKey.SPLIT_EXECUTOR)) {
            printExecutablePath(executablePath);
            return;
        }

        printGenericPath(path, () -> {});

        if (path.hasAttribute(AttributeKey.ACCESS_STACK)) {
            executeAccessStack.pop();
        }
    }

    private void printExecutablePath(ExecutablePath path) throws IOException {
        this.printGenericPath(path, () -> {
            // print the .executes method
            println();
            println(".executes(ctx -> {");
            incrementIndent();

            final ExecutorType executorType = path.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);
            if (executorType != ExecutorType.NONE) {
                printBlock("""
                        if (!(ctx.getSource().getExecutor() instanceof {} executor)) {
                            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                                Component.text("This command requires {} {} executor!")
                            )).create();
                        }""",
                    executorType.toString().charAt(0) + executorType.toString().toLowerCase().substring(1),
                    executorType == ExecutorType.ENTITY ? "an" : "a",
                    executorType.toString().toLowerCase()
                );
                println();
            }

            boolean instancePrint = true;
            CommandPath<?> parentPath = path;

            while ((parentPath = parentPath.getParent()) != null) {
                if (parentPath instanceof RecordPath recordPath) {
                    printWithRecord(path, recordPath);
                    instancePrint = false;
                    break;
                }
            }

            if (instancePrint) {
                printWithInstance(path);
            }

            println("return Command.SINGLE_SUCCESS;");
            decrementIndent();
            println("})");
        });
    }

    private void printWithInstance(ExecutablePath path) throws IOException {
        printExecutesMethodCall(path, Utils.getInstanceName(executeAccessStack));
    }

    private void printExecutesMethodCall(ExecutablePath path, String typeName) throws IOException {
        println("{}.{}(", typeName, path.getExecutesMethod().getSimpleName().toString());
        incrementIndent();

        // Arguments
        printExecutesArguments(path);

        decrementIndent();
        println(");");
    }

    private void printWithRecord(ExecutablePath path, RecordPath recordPath) throws IOException {
        final String typeName = Utils.getTypeName(path.getExecutesMethod().getEnclosingElement());

        if (recordPath.getArguments().isEmpty()) {
            println("final {} executorClass = new {}();", typeName, typeName);
        } else {
            println("final {} executorClass = new {}(", typeName, typeName);
            incrementIndent();
            printExecutorArguments(recordPath, false);
            decrementIndent();
            println(");");
        }

        printExecutesMethodCall(path, "executorClass");
    }

    private void printExecutesArguments(ExecutablePath path) throws IOException {
        final ExecutorType executorType = path.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);
        if (path.getArguments().isEmpty() && executorType == ExecutorType.NONE) {
            println("ctx.getSource().getSender()");
            return;
        }

        println("ctx.getSource().getSender(),");
        if (executorType != ExecutorType.NONE) {
            printIndent();
            switch (executorType) {
                case ENTITY, PLAYER -> print("executor");
            }

            if (path.getArguments().isEmpty()) {
                println();
                return;
            }

            print(",");
            println();
        }
        printExecutorArguments(path, false);
    }

    private void printExecutorArguments(final CommandPath<?> path, boolean forceTrailingComma) throws IOException {
        if (path.getAttributeNotNull(AttributeKey.INHERIT_PARENT_ARGS) && path.getParent() != null) {
            printExecutorArguments(path.getParent(), true);
        }

        final List<? extends CommandArgument> arguments = path.getArguments();
        for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
            final CommandArgument argument = arguments.get(i);

            printIndent();
            if (argument instanceof RequiredCommandArgument requiredArgument) {
                print(requiredArgument.getArgumentType().retriever());
            } else {
                print("\"{}\"", argument.getName());
            }

            if (i + 1 < argumentsSize || forceTrailingComma) {
                print(",");
            }

            println();
        }
    }

    private void printArgument(CommandArgument argument) throws IOException {
        switch (argument) {
            case LiteralCommandArgument literalArgument -> printLiteral(literalArgument);
            case RequiredCommandArgument requiredArgument -> printRequiredArg(requiredArgument);
            default -> throw new IllegalStateException("Unknown argument: " + argument);
        }
    }

    private void printLiteral(LiteralCommandArgument literalArg) throws IOException {
        print("Commands.literal(\"{}\")", literalArg.literal());
    }

    private void printRequiredArg(RequiredCommandArgument requiredArg) throws IOException {
        print("Commands.argument(\"{}\", {})",
            requiredArg.getName(),
            requiredArg.getArgumentType().initializer()
        );

        if (requiredArg.getSuggestionProvider() != null) {
            incrementIndent();
            println();
            printIndent();
            print(".suggests(" + requiredArg.getSuggestionProvider().getProvider() + ")");
            decrementIndent();
        }
    }

    private void printRequires(@Nullable CommandPath<?> path) throws IOException {
        if (path != null) {
            final List<Requirement> requirements = new ArrayList<>();

            final boolean operator = path.getAttributeNotNull(AttributeKey.REQUIRES_OP);
            final ExecutorType executorType;

            if (path.hasAttribute(AttributeKey.EXECUTOR_TYPE) && !path.getAttributeNotNull(AttributeKey.EXECUTOR_HANDLED)) {
                executorType = path.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);
            } else {
                executorType = ExecutorType.NONE;
            }

            final Requirement req = path.getAttribute(AttributeKey.REQUIREMENT);
            if (req != null) {
                requirements.add(req);
            }

            requirements.addAll(path.getAttributeNotNull(AttributeKey.PERMISSIONS)
                .stream()
                .map(Requirement::permission)
                .toList());

            if (!requirements.isEmpty()) {
                final String requirementString = Requirement.combine(requirements).getRequirementString(operator, executorType);
                if (!requirementString.isEmpty()) {
                    println();
                    printIndent();
                    print(".requires(source -> {})", requirementString);
                }
            }
        }
    }

    private <T extends CommandArgument> void printArguments(List<T> arguments, @Nullable CommandPath<?> printRequirements, InsidePrinter insidePrinter, boolean noStartingThen) throws IOException {
        if (arguments.isEmpty()) {
            insidePrinter.print();
            return;
        }

        T arg = arguments.removeFirst();
        if (!noStartingThen) {
            println();
            printIndent();
            print(".then(");
        }

        printArgument(arg);
        incrementIndent();

        printRequires(printRequirements);
        printArguments(arguments, null, insidePrinter, false);
        decrementIndent();

        if (!noStartingThen) {
            println(")");
        }
    }

    private <T extends CommandArgument> void printGenericPath(CommandPath<T> path, InsidePrinter insidePrinter) throws IOException {
        printArguments(new ArrayList<>(path.getArguments()), path, () -> {
            for (final CommandPath<?> child : path.getChildren()) {
                printPath(child);
            }

            insidePrinter.print();
        }, path.getParent() == null);
    }
    //</editor-fold>

    @FunctionalInterface
    private interface InsidePrinter {
        void print() throws IOException;
    }
}