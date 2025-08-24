package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePath;
import net.strokkur.commands.internal.intermediate.paths.RecordPath;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CommandTreePrinter extends AbstractPrinter {

    private final CommandPath<?> commandPath;

    public CommandTreePrinter(final int indent, final Writer writer, final CommandPath<?> commandPath) {
        super(indent, writer);
        this.commandPath = commandPath;
    }

    @Override
    public void print() throws IOException {

    }

    private void printPath(CommandPath<?> path) throws IOException {

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

            

            decrementIndent();
            println("return Command.SINGLE_SUCCESS;");
            println("})");
        });
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

    @FunctionalInterface
    private interface InsidePrinter {
        void print() throws IOException;
    }

    @FunctionalInterface
    private interface PathPrinter<T extends CommandArgument> {
        void print(T argument) throws IOException;
    }
}
