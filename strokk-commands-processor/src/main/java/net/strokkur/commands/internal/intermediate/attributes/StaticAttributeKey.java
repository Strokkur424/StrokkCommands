package net.strokkur.commands.internal.intermediate.attributes;

import org.jspecify.annotations.Nullable;

public record StaticAttributeKey<T>(String key, @Nullable T defaultValue) implements AttributeKey<T> {}
