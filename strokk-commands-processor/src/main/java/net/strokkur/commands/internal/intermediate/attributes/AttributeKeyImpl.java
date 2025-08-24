package net.strokkur.commands.internal.intermediate.attributes;

import org.jspecify.annotations.Nullable;

public record AttributeKeyImpl<T>(String key, @Nullable T defaultValue) implements AttributeKey<T> {}
