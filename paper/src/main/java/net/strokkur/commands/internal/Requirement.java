package net.strokkur.commands.internal;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@NullMarked
final class Requirement {

    public static final Requirement PERMISSION_OP = new Requirement("stack.getSender().isOp()");
    public static final Requirement NONE = new Requirement("");
    private final String requirementString;

    Requirement(String requirementString) {
        this.requirementString = requirementString;
    }

    private boolean handled = false;

    public static Requirement ofPermission(String permission) {
        return new Requirement("stack.getSender().hasPermission(\"%s\")".formatted(permission));
    }

    public static Requirement combine(@Nullable Requirement... requirements) {
        String permissionString = String.join(" && ", Stream.of(requirements)
            .filter(Objects::nonNull)
            .map(Requirement::getRequirementString)
            .filter(req -> !req.isBlank())
            .toList());

        if (permissionString.isBlank()) {
            return Requirement.NONE;
        }
        return new Requirement(permissionString);
    }

    public static Requirement either(List<Requirement> requirements) {
        List<Requirement> nonEmpty = requirements.stream()
            .filter(req -> !req.isEmpty())
            .toList();
        
        if (nonEmpty.size() <= 1) {
            if (nonEmpty.isEmpty()) {
                return Requirement.NONE;
            }
            return nonEmpty.getFirst();
        }
        
        String permissionString = String.join(" || ", nonEmpty.stream()
            .map(Requirement::getRequirementString)
            .map(req -> "(" + req + ")")
            .toList());

        if (permissionString.isBlank()) {
            return Requirement.NONE;
        }
        return new Requirement(permissionString);
    }

    public String getRequirementString() {
        return requirementString;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean value) {
        this.handled = value;
    }
    
    public boolean isEmpty() {
        return requirementString.isBlank();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Requirement that = (Requirement) obj;
        return Objects.equals(this.requirementString, that.requirementString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirementString);
    }

    @Override
    public String toString() {
        return "Requirement{" +
               "requirementString='" + requirementString + '\'' +
               ", handled=" + handled +
               '}';
    }
}