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

import net.strokkur.commands.internal.arguments.LiteralArgumentInfoImpl;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandTree extends CommandNode {

    public CommandTree(String name, Element classElement, List<Requirement> rootRequirements) {
        //noinspection DataFlowIssue
        super(null, new LiteralArgumentInfoImpl(name, classElement, name, false), name);
        this.getRequirements().addAll(rootRequirements);
    }

    public String printTreeAsBrigadier(int baseIndent) {
        // Before we do this, move the permissions around a bit
        this.visitEach(node -> {
            if (node instanceof CommandTree) {
                return;
            }

            Set<Requirement> requirements = node.getRequirements();
            CommandNode rootNode = node.getRootCommandNode();

            for (Requirement req : new HashSet<>(requirements)) {
                if (rootNode.getChildNodes().size() == 1) {
                    // We can just move it up since it doesn't branch
                    // But watch out for if it is an executing node, since we cannot do that then

                    if (!rootNode.hasExecutor()) {
                        rootNode.getRequirements().add(req);
                        requirements.remove(req);
                    }
                } else {
                    // There is branching going on. Since I do not want to deal with this yet, just don't move stuff up I guess
                    continue;
                }
            }
        });

        return printAsBrigadier(baseIndent);
    }

    @Override
    public void insert(ExecutorInformation executorInformation, MessagerWrapper messager) {
        super.insert(executorInformation, messager);
    }

    @Override
    public CommandNode getRootCommandNode() {
        throw new UnsupportedOperationException("A CommandTree has no root node.");
    }
}
