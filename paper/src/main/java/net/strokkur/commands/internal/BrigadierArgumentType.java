package net.strokkur.commands.internal;

import java.util.Objects;
import java.util.Set;

record BrigadierArgumentType(String initializer, String retriever, Set<String> imports) {

    public static BrigadierArgumentType of(String initializer, String retriever) {
        return new BrigadierArgumentType(initializer, retriever, Set.of());
    }
    
    public static BrigadierArgumentType of(String initializer, String retriever, String singleImport) {
        return new BrigadierArgumentType(initializer, retriever, Set.of(singleImport));
    }

    public static BrigadierArgumentType of(String initializer, String retriever, Set<String> imports) {
        return new BrigadierArgumentType(initializer, retriever, imports);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        BrigadierArgumentType that = (BrigadierArgumentType) o;
        return Objects.equals(retriever(), that.retriever()) && Objects.equals(initializer(), that.initializer()) && Objects.equals(imports(), that.imports());
    }

    @Override
    public int hashCode() {
        return Objects.hash(initializer(), retriever(), imports());
    }
}
