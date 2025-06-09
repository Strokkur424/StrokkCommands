package net.strokkur.commands.objects;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record ExecutorInformation(
    String methodName,
    ExecutorType type,
    String @Nullable [] initialLiterals,
    List<ArgumentInformation> arguments
) {
    @Override
    public String toString() {
        return "ExecutorInformation{" +
               "methodName='" + methodName + '\'' +
               ", type=" + type +
               ", initialLiterals=" + Arrays.toString(initialLiterals) +
               ", arguments=(" + String.join(",", arguments.stream().map(Objects::toString).toList()) + ")" +
               '}';
    }
}
