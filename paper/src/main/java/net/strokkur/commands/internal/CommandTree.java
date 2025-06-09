package net.strokkur.commands.internal;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CommandTree {

    private final Map<String, CommandTree> treeMap = new HashMap<>();
    private final ArgumentInformation argument;

    private @Nullable String name;
    private @Nullable ExecutorInformation executor = null;

    public CommandTree(ArgumentInformation argument) {
        this.argument = argument;
    }

    public void insert(ExecutorInformation executor) {
        insert(executor.arguments(), executor);
    }

    public void insert(List<ArgumentInformation> arguments, ExecutorInformation executor) {
        if (arguments.isEmpty()) {
            setExecutor(executor);
            return;
        }

        String[] names = arguments.getFirst() instanceof LiteralArgumentInformation litArg ? litArg.literals() : new String[]{arguments.getFirst().argumentName()};

        for (String name : names) {
            CommandTree next = treeMap.getOrDefault(name, new CommandTree(arguments.getFirst()));
            next.setName(name);

            List<ArgumentInformation> nextArguments = new ArrayList<>(arguments);
            nextArguments.removeFirst();
            next.insert(nextArguments, executor);

            treeMap.put(name, next);
        }
    }

    public @Nullable String getName() {
        return name;
    }

    public void setExecutor(@Nullable ExecutorInformation executor) {
        this.executor = executor;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (treeMap.isEmpty()) {
            return "CommandTree{" +
                   "argument=" + argument + "," +
                   "executes=" + (this.executor != null) + "}";
        }

        return "CommandTree{"
               + "children=[\n" + String.join("\n\t", treeMap.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).toList()) + "],"
               + "executes=" + (this.executor != null) + "}";
    }

    public String printAsBrigadier(int baseIndent) {
        String indent = "    ".repeat(baseIndent);
        String indentPlus = "    ".repeat(baseIndent + 1);
        String indentPlusPlus = "    ".repeat(baseIndent + 2);
        String indentPlusThree = "    ".repeat(baseIndent + 3);

        StringBuilder builder = new StringBuilder(argument instanceof RequiredArgumentInformation req
            ? "Commands.argument(\"%s\", %s)".formatted(this.argument.argumentName(), req.type().initializer())
            : "Commands.literal(\"%s\")".formatted(this.name));

        if (this.executor != null) {
            switch (this.executor.type()) {
                case ENTITY -> builder.append("\n").append(indentPlus).append(".requires(stack -> stack.getExecutor() != null)");
                case PLAYER -> builder.append("\n").append(indentPlus).append(".requires(stack -> stack.getExecutor() instanceof Player)");
            }
            builder.append("\n").append(indentPlus).append(".executes(ctx -> {\n");
            builder.append(indentPlusPlus).append("instance.%s(ctx.getSource().getSender()".formatted(this.executor.methodName()));

            switch (this.executor.type()) {
                case ENTITY -> builder.append(",\n").append(indentPlusThree).append("stack.getExecutor()");
                case PLAYER -> builder.append(",\n").append(indentPlusThree).append("(Player) stack.getExecutor()");
            }

            this.executor.arguments().stream()
                .filter(info -> info instanceof RequiredArgumentInformation)
                .map(info -> (RequiredArgumentInformation) info)
                .forEachOrdered(info -> builder.append(",\n").append(indentPlusThree).append(info.type().retriever()));
            builder.append("\n").append(indentPlusPlus).append(");");

            builder.append(indent).append("\n").append(indentPlusPlus).append("return Command.SINGLE_SUCCESS;\n");
            builder.append(indent).append("    })");
        }

        treeMap.values()
            .stream()
            .map(cmdTree -> cmdTree.printAsBrigadier(baseIndent + 1))
            .forEach(branch -> builder.append("\n").append(indent).append("    ")
                .append(".then(").append(branch)
                .append("\n").append(indent).append("    ").append(")"));

        return builder.toString();
    }
}
