package net.strokkur.commands.internal;

import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@NullMarked
final class Requirement {

    public static final Requirement IS_OP = new Requirement("stack.getSender().isOp()");

    private final String requirementString;

    Requirement(String requirementString) {
        this.requirementString = requirementString;
    }

    public static Requirement ofPermission(String permission) {
        return new Requirement("stack.getSender().hasPermission(\"%s\")".formatted(permission));
    }

    public static String stringOfAll(Collection<Requirement> requirements) {
        List<Requirement> nonEmpty = requirements.stream()
            .filter(req -> !req.isEmpty())
            .toList();

        if (nonEmpty.size() <= 1) {
            if (nonEmpty.isEmpty()) {
                return "";
            }
            return nonEmpty.getFirst().getRequirementString();
        }

        String permissionString = String.join(" && ", nonEmpty.stream()
            .map(Requirement::getRequirementString)
            .toList());

        if (permissionString.isBlank()) {
            return "";
        }
        return permissionString;
    }

    public static String stringOfEither(List<Requirement> requirements) {
        List<Requirement> nonEmpty = requirements.stream()
            .filter(req -> !req.isEmpty())
            .toList();

        if (nonEmpty.size() <= 1) {
            if (nonEmpty.isEmpty()) {
                return "";
            }
            return nonEmpty.getFirst().getRequirementString();
        }

        String permissionString = String.join(" || ", nonEmpty.stream()
            .map(Requirement::getRequirementString)
            .map(req -> "(" + req + ")")
            .toList());

        if (permissionString.isBlank()) {
            return "";
        }
        return permissionString;
    }

    public String getRequirementString() {
        return requirementString;
    }

    public boolean isEmpty() {
        return requirementString.isBlank();
    }

    public boolean notEmpty() {
        return !requirementString.isBlank();
    }

    @Override
    public String toString() {
        return "Requirement{" +
               "requirementString='" + requirementString + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Requirement that = (Requirement) o;
        return Objects.equals(requirementString, that.requirementString);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(requirementString);
    }
}