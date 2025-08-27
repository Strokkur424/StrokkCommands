package net.strokkur.commands.internal.intermediate.requirement;

import net.strokkur.commands.internal.intermediate.ExecutorType;

class PermissionRequirement implements Requirement {

    private final String permission;

    public PermissionRequirement(final String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public String getRequirementString(final boolean operator, final ExecutorType executorType) {
        final String defaultReq = Requirement.getDefaultRequirement(operator, executorType);
        final String permissionString = "source.getSender().hasPermission(\"%s\")".formatted(permission);

        if (defaultReq.isEmpty()) {
            return permissionString;
        }
        return defaultReq + " && " + permissionString;
    }

    @Override
    public String toString() {
        return "PermissionRequirement{" +
            "permission='" + permission + '\'' +
            '}';
    }
}
