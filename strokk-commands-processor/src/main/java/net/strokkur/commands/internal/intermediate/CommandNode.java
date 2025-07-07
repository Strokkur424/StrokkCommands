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
package net.strokkur.commands.internal.intermediate;

import net.strokkur.commands.internal.arguments.ArgumentInformation;
import net.strokkur.commands.internal.arguments.LiteralArgumentInfo;
import net.strokkur.commands.internal.arguments.LiteralArgumentInfoImpl;
import net.strokkur.commands.internal.arguments.RequiredArgumentInformation;
import net.strokkur.commands.internal.util.MessagerWrapper;
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
public class CommandNode {
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

    public void insert(ExecutorInformation executorInformation, MessagerWrapper messager) {
        insert(executorInformation.arguments(), executorInformation, messager);
    }

    public void insert(List<ArgumentInformation> arguments, ExecutorInformation executorInformation, MessagerWrapper messager) {
        if (arguments.isEmpty()) {
            if (hasExecutor()) {
                messager.errorElement("The defined command clashes with the command defined in '{}'!", executorInformation.methodElement(),
                    (getCurrentExecutor() != null ? getCurrentExecutor().methodElement() : "<unknown>")
                );
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
            messager.errorElement("Found argument of same name but different type! Duplicate argument found at: {}",
                nextArguments.getFirst().getElement(),
                next.getArgument().getElement()
            );
        }

        nextArguments.removeFirst();
        next.insert(nextArguments, executorInformation, messager);

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

    public String printAsBrigadier(int baseIndent) {
        String indent = "    ".repeat(baseIndent);
        String indentPlus = "    ".repeat(baseIndent + 1);
        String indentPlusPlus = "    ".repeat(baseIndent + 2);
        String indentPlusThree = "    ".repeat(baseIndent + 3);

        StringBuilder builder = new StringBuilder(argument instanceof RequiredArgumentInformation req
            ? "Commands.argument(\"%s\", %s)".formatted(this.argument.getArgumentName(), req.getType().initializer())
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
                    builder.append(info.getType().retriever());
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
