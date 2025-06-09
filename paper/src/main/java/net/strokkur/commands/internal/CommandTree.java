package net.strokkur.commands.internal;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;

@NullMarked
class CommandTree extends CommandNode {

    private final Requirement rootRequirement;

    public CommandTree(String name, Requirement rootRequirement) {
        super(new LiteralArgumentInformation(name, new String[]{name}), name);
        this.rootRequirement = rootRequirement;
    }

    public String printAsBrigadier(int baseIndent) {
        return printAsBrigadier(baseIndent, new ArrayList<>());
    }

    @Override
    protected Requirement getRequirementFor(ExecutorInformation executorInformation) {
        return Requirement.combine(rootRequirement, super.getRequirementFor(executorInformation));
    }
}
