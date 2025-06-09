package net.strokkur.commands.internal;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

record ExecutorInformation(
    String methodName,
    ExecutorType type,
    String @Nullable [] initialLiterals,
    List<ArgumentInformation> arguments,
    Requirement requirement
) {

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
}
