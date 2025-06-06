package net.strokkur.commands.objects;

import com.mojang.brigadier.arguments.ArgumentType;

import java.lang.reflect.Parameter;

public record ArgumentInformation(
    Class<?> type,
    Parameter parameter,
    ArgumentType<?> argumentType
) {
}
