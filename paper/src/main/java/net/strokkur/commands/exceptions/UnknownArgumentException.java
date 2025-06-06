package net.strokkur.commands.exceptions;

import java.lang.reflect.Method;

public class UnknownArgumentException extends RuntimeException {
    public UnknownArgumentException(Class<?> type, Class<?> command, Method method) {
        super(command.getName() + "#" + method.getName() + " declares an argument of type " + type.getSimpleName() + ", which isn't supported.");
    }
}
