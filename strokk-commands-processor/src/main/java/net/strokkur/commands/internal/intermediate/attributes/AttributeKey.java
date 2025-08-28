package net.strokkur.commands.internal.intermediate.attributes;

import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.requirement.Requirement;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public interface AttributeKey<T> {

    AttributeKey<ExecutorType> EXECUTOR_TYPE = create("executor_type", ExecutorType.NONE);
    AttributeKey<Boolean> EXECUTOR_HANDLED = create("executor_handled", false);

    AttributeKey<Requirement> REQUIREMENT = create("requirement", Requirement.EMPTY);
    AttributeKey<Boolean> REQUIRES_OP = create("requires_op", false);
    AttributeKey<Set<String>> PERMISSIONS = createDynamic("permission", HashSet::new);

    // Splitting
    AttributeKey<Boolean> INHERIT_PARENT_ARGS = create("inherit_parent_args", false);
    AttributeKey<Boolean> SPLIT_EXECUTOR = create("split_executor", false);

    @Contract(pure = true)
    String key();

    @Nullable
    @Contract(pure = true)
    T defaultValue();

    static <T> AttributeKey<T> create(String key, @Nullable T defaultValue) {
        return new StaticAttributeKey<>(key, defaultValue);
    }

    static <T> AttributeKey<T> createDynamic(String key, Supplier<@Nullable T> defaultValue) {
        return new DynamicAttributeKey<>(key, defaultValue);
    }
}
