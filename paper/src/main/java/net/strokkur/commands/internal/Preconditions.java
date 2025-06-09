package net.strokkur.commands.internal;

interface Preconditions {
    static void checkState(boolean state, String message) {
        if (!state) {
            throw new IllegalStateException(message);
        }
    }
}
