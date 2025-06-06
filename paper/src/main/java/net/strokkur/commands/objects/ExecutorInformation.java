package net.strokkur.commands.objects;

import java.lang.reflect.Method;
import java.util.List;

public record ExecutorInformation(
    Method method,
    ExecutorType type,
    List<ArgumentInformation> arguments
) {
}
