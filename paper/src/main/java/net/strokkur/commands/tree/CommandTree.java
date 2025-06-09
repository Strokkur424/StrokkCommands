package net.strokkur.commands.tree;

import net.strokkur.commands.objects.ArgumentInformation;
import net.strokkur.commands.objects.ExecutorInformation;
import net.strokkur.commands.objects.LiteralArgumentInformation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandTree {

    private final Map<String, CommandTree> treeMap = new HashMap<>();
    private final @Nullable ArgumentInformation argument;
    
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
        
        String[] names = arguments.getFirst() instanceof LiteralArgumentInformation litArg ? litArg.getLiterals() : new String[]{arguments.getFirst().getArgumentName()};

        for (String name : names) {
            CommandTree next = treeMap.getOrDefault(name, new CommandTree(arguments.getFirst()));
            
            List<ArgumentInformation> nextArguments = new ArrayList<>(arguments);
            nextArguments.removeFirst();
            next.insert(nextArguments, executor);
            
            treeMap.put(name, next);
        }
    }

    public void setExecutor(ExecutorInformation executor) {
        this.executor = executor;
    }

    @Override
    public String toString() {
        if (treeMap.isEmpty()) {
            return "CommandTree{" +
                   "argument=" + argument + "," + 
                   "executes=" + (this.executor != null) + "}";
        }
        
        return "CommandTree{"
            + "children=[" + String.join("\n\t", treeMap.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).toList()) + "],"
            + "executes=" + (this.executor != null) + "}";
    }
}