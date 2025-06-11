package net.strokkur.commands.internal.intermediate;

import org.jspecify.annotations.Nullable;

import java.util.List;

public enum ExecutorType {
    NONE,
    ENTITY("stack.getExecutor() != null"),
    PLAYER("stack.getExecutor() instanceof Player");

    private final @Nullable String predicate;

    ExecutorType() {
        this(null);
    }

    ExecutorType(@Nullable String predicate) {
        this.predicate = predicate;
    }

    public @Nullable String getPredicate() {
        return predicate;
    }

    public boolean hasPredicate() {
        return predicate != null;
    }

    public void addRequirement(List<Requirement> requirementList) {
        if (hasPredicate()) {
            requirementList.add(new Requirement(predicate));
        }
    }
}
