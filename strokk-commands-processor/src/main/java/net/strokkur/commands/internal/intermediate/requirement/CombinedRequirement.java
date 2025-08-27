package net.strokkur.commands.internal.intermediate.requirement;

import net.strokkur.commands.internal.intermediate.ExecutorType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CombinedRequirement implements Requirement {

    private final List<Requirement> requirements;

    public CombinedRequirement(final List<Requirement> requirements) {
        this.requirements = requirements;
    }

    private List<Requirement> flatten() {
        final List<Requirement> flattenedRequirements = new ArrayList<>();

        for (final Requirement child : requirements) {
            if (child instanceof CombinedRequirement combined) {
                flattenedRequirements.addAll(combined.flatten());
            } else {
                flattenedRequirements.add(child);
            }
        }

        return flattenedRequirements;
    }

    @Override
    public String getRequirementString() {
        final Set<String> permissions = new HashSet<>();
        ExecutorRequirement relevantExecutor = null;
        boolean operator = false;

        for (final Requirement req : flatten()) {
            switch (req) {
                case ExecutorRequirement executorRequirement -> {
                    if (relevantExecutor == null || executorRequirement.getExecutorType() == ExecutorType.NONE) {
                        continue;
                    }

                    if (executorRequirement.getExecutorType().isMoreRestrictiveThan(relevantExecutor.getExecutorType())) {
                        relevantExecutor = executorRequirement;
                    }
                }
                case OperatorRequirement ignored -> operator = true;
                case PermissionRequirement permissionRequirement -> permissions.add(permissionRequirement.getPermission());
                default -> {}
            }
        }

        final List<String> requirements = new ArrayList<>();
        if (relevantExecutor != null) {
            requirements.add(relevantExecutor.getRequirementString());
        }

        if (operator) {
            requirements.add(OperatorRequirement.OP_REQUIREMENT);
        }

        if (permissions.size() == 1) {
            requirements.add(permissions.iterator().next());
        } else if (permissions.size() > 1) {
            requirements.add("(" + String.join(" || ", permissions) + ")");
        }

        return String.join(" && ", requirements);
    }

    @Override
    public String toString() {
        return "CombinedRequirement{" +
            "requirements=" + requirements +
            '}';
    }
}
