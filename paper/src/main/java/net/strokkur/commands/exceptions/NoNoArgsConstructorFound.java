package net.strokkur.commands.exceptions;

public class NoNoArgsConstructorFound extends RuntimeException {
    public NoNoArgsConstructorFound(Class<?> clazz) {
        super("Command class " + clazz.getName() + " does not declare any non-arg constructor. Please register an already instantiated one instead!");
    }

    public NoNoArgsConstructorFound(Class<?> clazz, Throwable cause) {
        super("Command class " + clazz.getName() + " does not declare any non-arg constructor. Please register an already instantiated one instead!", cause);
    }
}
