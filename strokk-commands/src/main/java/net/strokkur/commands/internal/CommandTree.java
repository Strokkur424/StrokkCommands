package net.strokkur.commands.internal;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CommandTree extends CommandNode {

    public CommandTree(String name, Element classElement, List<Requirement> rootRequirements) {
        //noinspection DataFlowIssue
        super(null, new LiteralArgumentInformation(name, classElement, new String[]{name}), name);
        this.getRequirements().addAll(rootRequirements);
    }

    public String printAsBrigadier(int baseIndent) {
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

        return printAsBrigadier(baseIndent, new ArrayList<>());
    }

    @Override
    public void insert(ExecutorInformation executorInformation) {
        if (executorInformation.initialLiterals() != null) {
            for (int i = executorInformation.initialLiterals().length - 1; i >= 0; i--) {
                String literal = executorInformation.initialLiterals()[i];
                executorInformation.arguments().addFirst(new LiteralArgumentInformation(literal, executorInformation.methodElement(), new String[]{literal}, false));
            }
        }

        super.insert(executorInformation);
    }

    @Override
    public CommandNode getRootCommandNode() {
        throw new UnsupportedOperationException("A CommandTree has no root node.");
    }
}
