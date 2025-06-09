package net.strokkur.commands.internal;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
record BrigadierArgumentType(String initializer, String retriever, @Nullable String importString) {

    public static BrigadierArgumentType of(String initializer, String retriever) {
        return new BrigadierArgumentType(initializer, retriever, null);
    }

    public static BrigadierArgumentType of(String initializer, String retriever, @Nullable String importString) {
        return new BrigadierArgumentType(initializer, retriever, importString);
    }
}
