package net.strokkur.commands.internal.intermediate.requirement;

import net.strokkur.commands.internal.intermediate.ExecutorType;

import java.util.List;

public interface Requirement {

    Requirement EMPTY = (op, executorType) -> "Not handled anywhere, should not show up either.";

    String getRequirementString(boolean operator, ExecutorType executorType);

    static Requirement permission(String permission) {
        return new PermissionRequirement(permission);
    }

    static Requirement combine(List<Requirement> requirements) {
        return new CombinedRequirement(requirements);
    }

    static Requirement combine(Requirement... requirements) {
        return new CombinedRequirement(List.of(requirements));
    }

    //<editor-fold name="Utility Method"
    static String getDefaultRequirement(boolean operator, ExecutorType executorType) {
        if (!operator && executorType == ExecutorType.NONE) {
            return "";
        }

        if (operator && executorType == ExecutorType.NONE) {
            return "source.getSender().isOp()";
        }

        if (!operator) {
            return executorType.getPredicate();
        }

        return "source.getSender().isOp() && " + executorType.getPredicate();
    }
    //</editor-fold>
}
