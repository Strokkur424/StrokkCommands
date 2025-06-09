package net.strokkur.commands.internal;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NullMarked
class CommandNode {

    private final Map<String, CommandNode> childNodes = new HashMap<>();

    private final ArgumentInformation argument;
    private final String nodeName;

    private @Nullable ExecutorInformation currentExecutor = null;

    public CommandNode(ArgumentInformation argument, String nodeName) {
        this.argument = argument;
        this.nodeName = nodeName;
    }

    public void insert(ExecutorInformation executorInformation) {
        insert(executorInformation.arguments(), executorInformation);
    }

    public void insert(List<ArgumentInformation> arguments, ExecutorInformation executorInformation) {
        if (arguments.isEmpty()) {
            setCurrentExecutor(executorInformation);
            return;
        }

        String[] names = arguments.getFirst() instanceof LiteralArgumentInformation literalArgument
            ? literalArgument.literals()
            : new String[]{arguments.getFirst().argumentName()};

        for (String name : names) {
            CommandNode next = childNodes.getOrDefault(name, new CommandNode(arguments.getFirst(), name));

            List<ArgumentInformation> nextArguments = new ArrayList<>(arguments);
            nextArguments.removeFirst();
            next.insert(nextArguments, executorInformation);

            childNodes.put(name, next);
        }
    }

    public void visit(Consumer<CommandNode> nodeConsumer) {
        childNodes.values().forEach(node -> node.visit(nodeConsumer));
        nodeConsumer.accept(this);
    }

    public ArgumentInformation getArgument() {
        return argument;
    }

    public String getNodeName() {
        return nodeName;
    }

    public @Nullable ExecutorInformation getCurrentExecutor() {
        return currentExecutor;
    }

    public void setCurrentExecutor(@Nullable ExecutorInformation currentExecutor) {
        this.currentExecutor = currentExecutor;
    }
    
    protected Requirement getRequirement() {
        if (this.currentExecutor == null) {
            return Requirement.NONE;
        }
        
        Requirement out = this.currentExecutor.requirement();
        switch (this.currentExecutor.type()) {
            case ENTITY -> out = Requirement.combine(out, new Requirement("stack.getExecutor() != null"));
            case PLAYER -> out = Requirement.combine(out, new Requirement("stack.getExecutor() instanceof Player"));
        }
        return out;
    }

    protected String printAsBrigadier(int baseIndent, List<String> literalPosition) {
        String indent = "    ".repeat(baseIndent);
        String indentPlus = "    ".repeat(baseIndent + 1);
        String indentPlusPlus = "    ".repeat(baseIndent + 2);
        String indentPlusThree = "    ".repeat(baseIndent + 3);

        StringBuilder builder = new StringBuilder(argument instanceof RequiredArgumentInformation req
            ? "Commands.argument(\"%s\", %s)".formatted(this.argument.argumentName(), req.type().initializer())
            : "Commands.literal(\"%s\")".formatted(this.nodeName));

        Requirement status = getRequirement();
        if (status.requirementString() != null && !status.requirementString().isBlank()) {
            builder.append("\n").append(indentPlus).append(".requires(stack -> ")
                .append(status.requirementString())
                .append(")");
        }
        
        if (this.currentExecutor != null) {
            builder.append("\n").append(indentPlus).append(".executes(ctx -> {\n");
            builder.append(indentPlusPlus).append("INSTANCE.%s(\n".formatted(this.currentExecutor.methodName()))
                .append(indentPlusThree).append("ctx.getSource().getSender()");

            switch (this.currentExecutor.type()) {
                case ENTITY -> builder.append(",\n").append(indentPlusThree).append("ctx.getSource().getExecutor()");
                case PLAYER -> builder.append(",\n").append(indentPlusThree).append("(Player) ctx.getSource().getExecutor()");
            }

            List<String> literalsLeft = new ArrayList<>(literalPosition);
            for (ArgumentInformation arg : this.currentExecutor.arguments()) {
                builder.append(",\n").append(indentPlusThree);
                if (arg instanceof RequiredArgumentInformation info) {
                    builder.append(info.type().retriever());
                } else if (arg instanceof LiteralArgumentInformation) {
                    builder.append('"').append(literalsLeft.removeFirst()).append('"');
                }
            }
            builder.append("\n").append(indentPlusPlus).append(");");

            builder.append(indent).append("\n").append(indentPlusPlus).append("return Command.SINGLE_SUCCESS;\n");
            builder.append(indent).append("    })");
        }

        if (argument instanceof LiteralArgumentInformation) {
            literalPosition.add(this.nodeName);
        }

        childNodes.values()
            .stream()
            .map(cmdTree -> cmdTree.printAsBrigadier(baseIndent + 1, literalPosition))
            .forEach(branch -> builder.append("\n").append(indent).append("    ")
                .append(".then(").append(branch)
                .append("\n").append(indent).append("    ").append(")"));

        return builder.toString();
    }
}
