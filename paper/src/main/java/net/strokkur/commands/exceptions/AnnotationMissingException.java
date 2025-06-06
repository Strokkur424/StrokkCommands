package net.strokkur.commands.exceptions;

public class AnnotationMissingException extends RuntimeException {
    public AnnotationMissingException(Class<?> annotation, Class<?> clazz) {
        super(clazz.getName() + " does not declare the required annotation " + annotation.getSimpleName());
    }
}
