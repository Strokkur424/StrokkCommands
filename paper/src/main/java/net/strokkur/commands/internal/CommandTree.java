package net.strokkur.commands.internal;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;

@NullMarked
class CommandTree extends CommandNode {

    private final Requirement requirement;

    public CommandTree(String name, Requirement requirement) {
        super(new LiteralArgumentInformation(name, new String[]{name}), name);
        this.requirement = requirement;
    }

    public String printAsBrigadier(int baseIndent) {
        return printAsBrigadier(baseIndent, new ArrayList<>());
    }

    @Override
    protected Requirement getRequirement() {
        return Requirement.combine(requirement, super.getRequirement());
    }
}
