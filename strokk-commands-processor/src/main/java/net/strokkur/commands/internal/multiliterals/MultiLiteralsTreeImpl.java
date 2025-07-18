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
package net.strokkur.commands.internal.multiliterals;

import net.strokkur.commands.internal.arguments.ArgumentInformation;
import net.strokkur.commands.internal.arguments.LiteralArgumentInfo;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MultiLiteralsTreeImpl implements MultiLiteralsTree {
    private final ArgumentNode rootNode;

    public MultiLiteralsTreeImpl() {
        this.rootNode = new ArgumentNode(null, null);
    }

    @Override
    public void insert(ArgumentInformation single) {
        this.rootNode.insert(List.of(single));
    }

    @Override
    public void insert(LiteralArgumentInfo base, List<String> literals) {
        this.rootNode.insert(literals.stream()
            .map(base::withLiteral)
            .map(literalInfo -> (ArgumentInformation) literalInfo)
            .toList());
    }

    @Override
    public List<List<ArgumentInformation>> flatten() {
        return this.rootNode.flatten().stream()
            .map(list -> list.stream()
                .map(ArgumentNode::getArgumentInformation)
                .filter(Objects::nonNull)
                .toList())
            .toList();
    }

    private static final class ArgumentNode {

        private final @Nullable ArgumentInformation argumentInformation;
        private final @Nullable ArgumentNode parentNode;

        private final List<ArgumentNode> nextNodes = new ArrayList<>();

        public ArgumentNode(@Nullable ArgumentNode parentNode, @Nullable ArgumentInformation info) {
            this.parentNode = parentNode;
            this.argumentInformation = info;
        }

        public void insert(List<ArgumentInformation> information) {
            if (nextNodes.isEmpty()) {
                for (ArgumentInformation info : information) {
                    nextNodes.add(new ArgumentNode(this, info));
                }
                return;
            }

            nextNodes.forEach(node -> node.insert(information));
        }

        public List<List<ArgumentNode>> flatten() {
            List<List<ArgumentNode>> allNodes = new ArrayList<>();

            visitAllLeaves(node -> {
                List<ArgumentNode> argumentNodes = new ArrayList<>();
                ArgumentNode currentNode = node;

                do {
                    argumentNodes.add(currentNode);
                    currentNode = currentNode.parentNode;
                } while (currentNode != null && currentNode.parentNode != null);

                allNodes.add(argumentNodes.reversed());
            });

            return allNodes;
        }

        public @Nullable ArgumentInformation getArgumentInformation() {
            return argumentInformation;
        }

        private void visitAllLeaves(Consumer<ArgumentNode> nodeConsumer) {
            if (this.nextNodes.isEmpty()) {
                nodeConsumer.accept(this);
                return;
            }

            this.nextNodes.forEach(node -> node.visitAllLeaves(nodeConsumer));
        }
    }
}
