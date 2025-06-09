package net.strokkur.commands.internal;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

record BrigadierArgumentType(String initializer, String retriever, @Nullable String importString) {

    public static BrigadierArgumentType of(String initializer, String retriever) {
        return new BrigadierArgumentType(initializer, retriever, null);
    }

    public static BrigadierArgumentType of(String initializer, String retriever, @Nullable String importString) {
        return new BrigadierArgumentType(initializer, retriever, importString);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BrigadierArgumentType that = (BrigadierArgumentType) o;
        return Objects.equals(retriever(), that.retriever()) && Objects.equals(initializer(), that.initializer()) && Objects.equals(importString(), that.importString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(initializer(), retriever(), importString());
    }
}
