package net.strokkur.commands.internal;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
class CommandNode {

    private final Map<String, CommandNode> childNodes = new HashMap<>();

    private final ArgumentInformation argument;
    private final String nodeName;
    private final Set<Requirement> requirements = new HashSet<>();
    private final CommandNode rootCommandNode;

    private @Nullable ExecutorInformation currentExecutor = null;

    public CommandNode(CommandNode rootCommandNode, ArgumentInformation argument, String nodeName) {
        this.rootCommandNode = rootCommandNode;
        this.argument = argument;
        this.nodeName = nodeName;
    }

    public void insert(ExecutorInformation executorInformation) {
        insert(executorInformation.arguments(), executorInformation);
    }

    public void insert(List<ArgumentInformation> arguments, ExecutorInformation executorInformation) {
        if (arguments.isEmpty()) {
            if (hasExecutor()) {
                StrokkCommandsPreprocessor.getMessenger().ifPresent(messager -> messager.printError(
                    "The defined command clashes with the command defined in '"
                    + (getCurrentExecutor() != null ? getCurrentExecutor().methodElement() : null)
                    + "'", executorInformation.methodElement()
                ));
            }

            setCurrentExecutor(executorInformation);
            getRequirements().addAll(executorInformation.requirements());
            return;
        }
        
        ArgumentInformation first = arguments.getFirst();

        String name = first instanceof LiteralArgumentInfo lit ? lit.getLiteral() : first.getArgumentName();
        CommandNode next = childNodes.getOrDefault(name, new CommandNode(this, first, name));
        List<ArgumentInformation> nextArguments = new ArrayList<>(arguments);

        if (!next.getArgument().equals(nextArguments.getFirst())) {
            StrokkCommandsPreprocessor.getMessenger().ifPresent(messager -> {
                next.getArgument().getElement();
                messager.printError(
                    "Found argument of same name but different type! Duplicate argument found at: " + next.getArgument().getElement(),
                    nextArguments.getFirst().getElement()
                );
            });
        }

        nextArguments.removeFirst();
        next.insert(nextArguments, executorInformation);

        childNodes.put(name, next);
    }

    public void visitEach(Consumer<CommandNode> nodeConsumer) {
        childNodes.values().forEach(node -> node.visitEach(nodeConsumer));
        nodeConsumer.accept(this);
    }

    public Map<String, CommandNode> getChildNodes() {
        return childNodes;
    }

    public CommandNode getRootCommandNode() {
        return rootCommandNode;
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

    protected Set<Requirement> getRequirements() {
        return this.requirements;
    }

    public boolean hasExecutor() {
        return this.currentExecutor != null;
    }

    protected String printAsBrigadier(int baseIndent) {
        String indent = "    ".repeat(baseIndent);
        String indentPlus = "    ".repeat(baseIndent + 1);
        String indentPlusPlus = "    ".repeat(baseIndent + 2);
        String indentPlusThree = "    ".repeat(baseIndent + 3);

        StringBuilder builder = new StringBuilder(argument instanceof RequiredArgumentInformation req
            ? "Commands.argument(\"%s\", %s)".formatted(this.argument.getArgumentName(), req.type().initializer())
            : "Commands.literal(\"%s\")".formatted(this.nodeName));

        setRequirement(builder, indentPlus);
        
        if (argument.getSuggestionProvider() != null) {
            builder.append("\n").append(indentPlus).append(".suggests(").append(argument.getSuggestionProvider().get()).append(")");
        }

        if (this.currentExecutor != null) {
            builder.append("\n").append(indentPlus).append(".executes(ctx -> {\n");
            builder.append(indentPlusPlus).append("INSTANCE.%s(\n".formatted(this.currentExecutor.methodName()))
                .append(indentPlusThree).append("ctx.getSource().getSender()");

            switch (this.currentExecutor.type()) {
                case ENTITY -> builder.append(",\n").append(indentPlusThree).append("ctx.getSource().getExecutor()");
                case PLAYER -> builder.append(",\n").append(indentPlusThree).append("(Player) ctx.getSource().getExecutor()");
            }

            for (ArgumentInformation arg : this.currentExecutor.arguments()) {
                if (arg instanceof LiteralArgumentInfoImpl info) {
                    if (info.addToMethod()) {
                        builder.append(",\n").append(indentPlusThree);
                        builder.append('"').append(info.getLiteral()).append('"');
                    }
                    continue;
                }

                if (arg instanceof RequiredArgumentInformation info) {
                    builder.append(",\n").append(indentPlusThree);
                    builder.append(info.type().retriever());
                }
            }

            builder.append("\n").append(indentPlusPlus).append(");\n");

            builder.append(indentPlusPlus).append("return Command.SINGLE_SUCCESS;\n");
            builder.append(indent).append("    })");
        }

        childNodes.values()
            .stream()
            .map(cmdTree -> cmdTree instanceof CommandTree tree ? tree.printTreeAsBrigadier(baseIndent + 1) : cmdTree.printAsBrigadier(baseIndent + 1))
            .forEach(branch -> builder.append("\n").append(indent).append("    ")
                .append(".then(").append(branch)
                .append("\n").append(indent).append("    ").append(")"));

        return builder.toString();
    }

    private void setRequirement(StringBuilder builder, String indentPlus) {
        if (getRequirements().isEmpty()) {
            return;
        }

        String compiled = Requirement.stringOfAll(getRequirements());
        builder.append("\n").append(indentPlus).append(".requires(stack -> ")
            .append(compiled)
            .append(")");
    }
}
