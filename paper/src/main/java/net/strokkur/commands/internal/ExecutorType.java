package net.strokkur.commands.internal;

enum ExecutorType {
    NONE("true"),
    ENTITY("stack.getExecutor() != null"),
    PLAYER("stack.getExecutor() instanceof Player");

    private final String predicate;

    ExecutorType(String predicate) {
        this.predicate = predicate;
    }

    public String getPredicate() {
        return predicate;
    }
}
