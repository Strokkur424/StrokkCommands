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
    private final List<Requirement> requirements;

    ExecutorInformation(
        String methodName,
        ExecutorType type,
        String @Nullable [] initialLiterals,
        List<ArgumentInformation> arguments,
        List<Requirement> requirements
    ) {
        this.methodName = methodName;
        this.type = type;
        this.initialLiterals = initialLiterals;
        this.arguments = arguments;
        this.requirements = requirements;
    }

    public String getMethodName() {
        return methodName;
    }

    public ExecutorType getType() {
        return type;
    }

    public String @Nullable [] getInitialLiterals() {
        return initialLiterals;
    }

    public List<ArgumentInformation> getArguments() {
        return arguments;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecutorInformation that = (ExecutorInformation) o;
        return Objects.equals(methodName, that.methodName) && type == that.type && Objects.deepEquals(initialLiterals, that.initialLiterals) && Objects.equals(arguments, that.arguments) && Objects.equals(requirements, that.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, type, Arrays.hashCode(initialLiterals), arguments, requirements);
    }

    @Override
    public String toString() {
        return "ExecutorInformation{" +
               "methodName='" + methodName + '\'' +
               ", type=" + type +
               ", initialLiterals=" + Arrays.toString(initialLiterals) +
               ", arguments=" + arguments +
               ", requirements=" + requirements +
               '}';
    }
}
