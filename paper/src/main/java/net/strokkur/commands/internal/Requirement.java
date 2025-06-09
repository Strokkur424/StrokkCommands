package net.strokkur.commands.internal;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

@NullMarked
record Requirement(@Nullable String requirementString) {

    public static final Requirement PERMISSION_OP = new Requirement("stack.getSender().isOp()");
    public static final Requirement NONE = new Requirement(null);

    public static Requirement ofPermission(String permission) {
        return new Requirement("stack.getSender().hasPermission(\"%s\")".formatted(permission));
    }

    public static Requirement combine(@Nullable Requirement... statuses) {
        String permissionString = String.join(" && ", Stream.of(statuses)
            .filter(Objects::nonNull)
            .map(Requirement::requirementString)
            .filter(Objects::nonNull)
            .toList());

        if (permissionString.isBlank()) {
            return Requirement.NONE;
        }
        return new Requirement(permissionString);
    }

    @Override
    public String toString() {
        return "PermissionStatus{" +
               "requirementString='" + requirementString + '\'' +
               '}';
    }
}
