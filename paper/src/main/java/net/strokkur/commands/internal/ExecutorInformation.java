package net.strokkur.commands.internal;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class ExecutorInformation {
    private final String methodName;
    private final ExecutorType type;
    private final String @Nullable [] initialLiterals;
    private final List<ArgumentInformation> arguments;
    private final Requirement initialRequirement;

    private Requirement requirement;
    private boolean compiledRequirement = false;

    ExecutorInformation(
        String methodName,
        ExecutorType type,
        String @Nullable [] initialLiterals,
        List<ArgumentInformation> arguments,
        Requirement requirement
    ) {
        this.methodName = methodName;
        this.type = type;
        this.initialLiterals = initialLiterals;
        this.arguments = arguments;
        this.requirement = requirement;
        this.initialRequirement = requirement;
    }

    public void setRequirement(Requirement requirement) {
        Preconditions.checkState(!compiledRequirement, "Cannot recompile already compiled requirements");
        this.requirement = requirement;
        compiledRequirement = true;
    }

    public boolean isCompiledRequirement() {
        return compiledRequirement;
    }

    @Override
    public String toString() {
        return "ExecutorInformation{" +
               "methodName='" + methodName + '\'' +
               ", type=" + type +
               ", initialLiterals=" + Arrays.toString(initialLiterals) +
               ", arguments=(" + String.join(",", arguments.stream().map(Objects::toString).toList()) + ")" +
               ", permissionStatus=(" + requirement + ")" +
               '}';
    }

    public String methodName() {
        return methodName;
    }

    public ExecutorType type() {
        return type;
    }

    public String @Nullable [] initialLiterals() {
        return initialLiterals;
    }

    public List<ArgumentInformation> arguments() {
        return arguments;
    }

    public Requirement requirement() {
        return requirement;
    }

    public Requirement getInitialRequirement() {
        return initialRequirement;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (ExecutorInformation) obj;
        return Objects.equals(this.methodName, that.methodName) &&
               Objects.equals(this.type, that.type) &&
               Arrays.equals(this.initialLiterals, that.initialLiterals) &&
               Objects.equals(this.arguments, that.arguments) &&
               Objects.equals(this.requirement, that.requirement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, type, Arrays.hashCode(initialLiterals), arguments, requirement);
    }

}
