package net.strokkur.commands.internal.intermediate.requirement;

import net.strokkur.commands.internal.intermediate.ExecutorType;

class ExecutorRequirement implements Requirement {

    private final ExecutorType executorType;

    public ExecutorRequirement(final ExecutorType executorType) {
        this.executorType = executorType;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    @Override
    public String getRequirementString() {
        if (executorType == ExecutorType.NONE) {
            return "";
        }

        return "source.getExecutor() instanceof " + switch (executorType) {
            case ENTITY -> "Entity";
            case PLAYER -> "Player";
            default -> throw new IllegalStateException("Unknown executor type: " + executorType);
        };
    }

    @Override
    public String toString() {
        return "ExecutorRequirement{" +
            "executorType=" + executorType +
            '}';
    }
}
