package net.strokkur.commands.internal.intermediate.requirement;

class PermissionRequirement implements Requirement {

    private final String permission;

    public PermissionRequirement(final String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public String getRequirementString() {
        return "source.hasPermission(\"" + permission + "\")";
    }

    @Override
    public String toString() {
        return "PermissionRequirement{" +
            "permission='" + permission + '\'' +
            '}';
    }
}
