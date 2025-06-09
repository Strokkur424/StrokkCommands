package net.strokkur.commands.internal;

class UnknownArgumentException extends RuntimeException {
    public UnknownArgumentException(String type) {
        super("An argument of type " + type + " currently isn't supported.");
    }
}
