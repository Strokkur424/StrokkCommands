package net.strokkur.commands.internal.intermediate.requirement;

class OperatorRequirement implements Requirement {

    public static final String OP_REQUIREMENT = "source.getSender().isOp()";

    @Override
    public String getRequirementString() {
        return OP_REQUIREMENT;
    }

    @Override
    public String toString() {
        return "OperatorRequirement{}";
    }
}
