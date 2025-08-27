package net.strokkur.commands.internal.intermediate.requirement;

import net.strokkur.commands.internal.intermediate.ExecutorType;

import java.util.List;

public interface Requirement {

    Requirement OPERATOR = new OperatorRequirement();

    Requirement EMPTY = () -> "";

    String getRequirementString();

    static Requirement executor(ExecutorType executor) {
        return new ExecutorRequirement(executor);
    }

    static Requirement permission(String permission) {
        return new PermissionRequirement(permission);
    }

    static Requirement combine(List<Requirement> requirements) {
        return new CombinedRequirement(requirements);
    }

    static Requirement combine(Requirement... requirements) {
        return new CombinedRequirement(List.of(requirements));
    }
}
