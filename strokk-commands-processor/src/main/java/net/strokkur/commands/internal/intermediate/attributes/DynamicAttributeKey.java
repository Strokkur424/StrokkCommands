package net.strokkur.commands.internal.intermediate.attributes;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public record DynamicAttributeKey<T>(String key, Supplier<@Nullable T> supplier) implements AttributeKey<T> {

    @Override
    public @Nullable T defaultValue() {
        return supplier.get();
    }
}
