package net.strokkur.commands.internal.intermediate.attributes;

import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.Requirement;
import net.strokkur.commands.internal.intermediate.SuggestionProvider;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface AttributeKey<T> {

    AttributeKey<ExecutorType> EXECUTOR_TYPE = create("executor_type", ExecutorType.NONE);
    AttributeKey<Boolean> EXECUTOR_HANDLED = create("executor_handled", false);
    AttributeKey<Requirement> REQUIREMENT = create("requirement", null);
    AttributeKey<SuggestionProvider> SUGGESTION_PROVIDER = create("suggestion_provider", null);
    AttributeKey<Boolean> REQUIRES_OP = create("requires_op", false);
    AttributeKey<String> PERMISSION = create("permission", null);

    @Contract(pure = true)
    String key();

    @Nullable
    @Contract(pure = true)
    T defaultValue();

    static <T> AttributeKey<T> create(String key, @Nullable T defaultValue) {
        return new AttributeKeyImpl<>(key, defaultValue);
    }
}
