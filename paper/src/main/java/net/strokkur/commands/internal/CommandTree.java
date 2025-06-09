package net.strokkur.commands.internal;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CommandTree {

    private final Map<String, CommandTree> treeMap = new HashMap<>();
    private final @Nullable ArgumentInformation argument;

    private @Nullable String name;
    private @Nullable ExecutorInformation executor = null;

    public CommandTree(@Nullable ArgumentInformation argument) {
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

    public String printAsBrigadier() {
        throw new UnsupportedOperationException("Not implemented");
    }
}