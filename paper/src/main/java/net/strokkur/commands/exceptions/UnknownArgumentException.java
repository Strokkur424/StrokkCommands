package net.strokkur.commands.exceptions;

public class UnknownArgumentException extends RuntimeException {
    public UnknownArgumentException(String type) {
        super("An argument of type " + type + " currently isn't supported.");
    }
}
