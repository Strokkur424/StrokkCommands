package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.BuildConstants;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePath;
import net.strokkur.commands.internal.intermediate.paths.LiteralCommandPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPath;
import net.strokkur.commands.internal.util.Classes;
import org.jetbrains.annotations.UnmodifiableView;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandTreePrinter extends AbstractPrinter {

    private static final Set<String> STANDARD_IMPORTS = Set.of(
        Classes.COMMAND,
        Classes.LITERAL_COMMAND_NODE,
        Classes.COMMAND_SOURCE_STACK,
        Classes.COMMANDS
    );

    private final CommandPath<?> commandPath;
    private final CommandInformation commandInformation;

    public CommandTreePrinter(final int indent, final Writer writer, final CommandPath<?> commandPath, final CommandInformation commandInformation) {
        super(indent, writer);
        this.commandPath = commandPath;
        this.commandInformation = commandInformation;
    }

    private String getTypeVariableName(Element type) {
        final List<String> names = new ArrayList<>(16);

        do {
            names.add(type.getSimpleName().toString().toUpperCase(Locale.ROOT));
            type = type.getEnclosingElement();
        } while (type instanceof TypeElement);

        final StringBuilder builder = new StringBuilder();
        for (final String name : names.reversed()) {
            builder.append(name).append("_");
        }
        return builder.toString();
    }

    private String getTypeName(Element type) {
        final List<String> names = new ArrayList<>(16);

        do {
            names.add(type.getSimpleName().toString());
            type = type.getEnclosingElement();
        } while (type instanceof TypeElement);

        final StringBuilder builder = new StringBuilder();
        for (final String name : names.reversed()) {
            builder.append(name).append(".");
        }
        return builder.toString();
    }

    @Override
    public void print() throws IOException {
        final Set<String> imports = new HashSet<>(STANDARD_IMPORTS);
        gatherImports(imports, commandPath);

        final String packageName = ((PackageElement) commandInformation.classElement().getEnclosingElement()).getQualifiedName().toString();
        final String brigadierClassName = getTypeName(commandInformation.classElement()) + "Brigadier";

        final String description = commandInformation.description() == null ? "null" : commandInformation.description();
        final String aliases = commandInformation.aliases() == null ? "" : '"' + String.join("\", \"", List.of(commandInformation.aliases())) + '"';

        println("package {};", packageName);
        println();
        printImports(imports);
        println();

        printBlock("""
                /**
                 * A class holding the Brigadier source tree generated from
                 * {@link {}} using <a href="https://commands.strokkur.net>StrokkCommands</a>.
                 *
                 * @author Strokkur24 - StrokkCommands
                 * @version {}
                 * @see #create() Creating the LiteralArgumentNode.
                 * @see #register(Commands) Registering the command.
                 */""",
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
                 * {@link io.papermc.paper.plugin.bootstrap.PluginBootstrap#bootstrap(BootstrapContext)}, your main
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
                    commands.register(create(), {}, {});
                }""",
            brigadierClassName,
            description,
            aliases
        );

        println();

        printBlock("""
            /**
             * A method for creating a Brigadier command node which denotes the declared command
             * in {@link {}). You can either retrieve the unregistered node with this method
             * or register it directly with {@link #register(Commands)}.
             */
            public static LiteralCommandNode<CommandSourceStack> create() {""");
        incrementIndent();
        print("return ");
        printPath(commandPath);
        println(";");
        decrementIndent();
        println("}");

        decrementIndent();
        println("}");
    }

    public void printImports(Set<String> imports) throws IOException {
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

    private void gatherImports(Set<String> imports, CommandPath<?> commandPath) {
        if (!(commandPath instanceof LiteralCommandPath)) {
            for (final CommandArgument arg : commandPath.getArguments()) {
                if (arg instanceof RequiredCommandArgument requiredArgument) {
                    imports.addAll(requiredArgument.getArgumentType().imports());
                }
            }
        }

        for (final CommandPath<?> child : commandPath.getChildren()) {
            gatherImports(imports, child);
        }
    }

    //<editor-fold name="Command Tree Printing Methods">
    private void printPath(CommandPath<?> path) throws IOException {
        switch (path) {
            case RecordPath recordPath -> printRecordPath(recordPath);
            case ExecutablePath executablePath -> printExecutablePath(executablePath);
            case LiteralCommandPath literalPath -> printLiteralPath(literalPath);
            default -> throw new IllegalStateException("Unknown path: " + path);
        }
    }

    private void printExecutablePath(ExecutablePath path) throws IOException {
        printGenericPath(path, arg -> {
            if (arg instanceof RequiredCommandArgument requiredArgument) {
                printRequiredArg(requiredArgument);
            } else if (arg instanceof LiteralCommandArgument literalArgument) {
                printLiteral(literalArgument);
            } else {
                throw new IllegalArgumentException("Unknown argument type: " + arg.getClass());
            }
        }, () -> {
            // print the .executes method
            println(".executes(ctx -> {");
            incrementIndent();

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

            decrementIndent();
            println("return Command.SINGLE_SUCCESS;");
            println("})");
        });
    }

    private void printWithInstance(ExecutablePath path) throws IOException {
        final TypeElement typeElement = (TypeElement) path.getExecutesMethod().getEnclosingElement();
        printExecutesMethodCall(path, getTypeVariableName(typeElement));
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
        if (recordPath.getArguments().isEmpty()) {
            println("{} executorClass = new {}();");
        } else {
            println("{} executorClass = new {}(");
            incrementIndent();
            printArguments(recordPath.getArguments());
            decrementIndent();
            println(");");
        }

        printExecutesMethodCall(path, "executorClass");
    }

    private void printExecutesArguments(ExecutablePath path) throws IOException {
        if (path.getArguments().isEmpty()) {
            println("ctx.getSource().getSender()");
            return;
        }

        println("ctx.getSource().getSender(),");
        printArguments(path.getArguments());
    }

    private void printArguments(List<? extends CommandArgument> arguments) throws IOException {
        for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
            final CommandArgument argument = arguments.get(i);

            if (argument instanceof RequiredCommandArgument requiredArgument) {
                print(requiredArgument.getArgumentType().retriever());
            } else {
                print("\"{}\"", argument.getName());
            }

            if (i + 1 < argumentsSize) {
                println(",");
            } else {
                println();
            }
        }
    }

    private void printLiteralPath(CommandPath<LiteralCommandArgument> literalPath) throws IOException {
        printGenericPath(literalPath, this::printLiteral);
    }

    private void printRecordPath(RecordPath recordPath) throws IOException {
        printGenericPath(recordPath, this::printRequiredArg);
    }

    private void printLiteral(LiteralCommandArgument literalArg) throws IOException {
        print("Commands.literal(\"{}\")", literalArg.getLiteral());
    }

    private void printRequiredArg(RequiredCommandArgument requiredArg) throws IOException {
        print("Commands.argument(\"{}\", {})",
            requiredArg.getName(),
            requiredArg.getArgumentType().initializer()
        );
    }

    private <T extends CommandArgument> void printGenericPath(CommandPath<T> path, PathPrinter<T> printer, InsidePrinter insidePrinter) throws IOException {
        @UnmodifiableView List<T> arguments = path.getArguments();
        for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
            final T arg = arguments.get(i);

            printer.print(arg);
            println();
            incrementIndent();

            if (i + 1 < argumentsSize) {
                print(".then(");
            }
        }

        for (final CommandPath<?> child : path.getChildren()) {
            printPath(child);
        }

        insidePrinter.print();

        for (int i = 1, argumentsSize = arguments.size(); i < argumentsSize; i++) {
            println();
            decrementIndent();
            print(")");
        }
    }

    private <T extends CommandArgument> void printGenericPath(CommandPath<T> path, PathPrinter<T> printer) throws IOException {
        printGenericPath(path, printer, () -> {});
    }
    //</editor-fold>

    @FunctionalInterface
    private interface InsidePrinter {
        void print() throws IOException;
    }

    @FunctionalInterface
    private interface PathPrinter<T extends CommandArgument> {
        void print(T argument) throws IOException;
    }
}