package net.strokkur.commands.internal.intermediate.requirement;

import net.strokkur.commands.internal.intermediate.ExecutorType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CombinedRequirement implements Requirement {

    private final List<Requirement> requirements;

    public CombinedRequirement(final List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public static void concatPermissions(Requirement req, Set<String> handled) {
        if (req instanceof CombinedRequirement comb) {
            for (final Requirement requirement : comb.requirements) {
                concatPermissions(requirement, handled);
            }
        }

        if (req instanceof PermissionRequirement permissionRequirement) {
            handled.add(permissionRequirement.getPermission());
        }
    }

    @Override
    public String getRequirementString(boolean operator, ExecutorType executorType) {
        final Set<String> permissions = new HashSet<>();
        concatPermissions(this, permissions);

        final String defaultReq = Requirement.getDefaultRequirement(operator, executorType);
        if (permissions.isEmpty()) {
            return defaultReq;
        }

        final String permissionsString = String.join(" || ", permissions.stream()
            .map("source.getSender().hasPermission(\"%s\")"::formatted)
            .toList());

        if (defaultReq.isBlank()) {
            return permissionsString;
        }

        final String paranthesisPermissionString;
        if (permissions.size() == 1) {
            paranthesisPermissionString = permissionsString;
        } else {
            paranthesisPermissionString = "(" + permissionsString + ")";
        }

        return defaultReq + " && " + paranthesisPermissionString;
    }

    @Override
    public String toString() {
        return "CombinedRequirement{" +
            "requirements=" + requirements +
            '}';
    }
}
