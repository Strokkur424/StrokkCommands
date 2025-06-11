package net.strokkur.commands.internal.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

class MessagerWrapperImpl implements MessagerWrapper {

    private final Messager messager;

    public MessagerWrapperImpl(Messager messager) {
        this.messager = messager;
    }

    @Override
    public void info(String format, Object... arguments) {
        messager.printNote(format.replaceAll("\\{}", "%s").formatted(arguments));
    }

    @Override
    public void infoElement(String format, Element element, Object... arguments) {
        messager.printNote(format.replaceAll("\\{}", "%s").formatted(arguments), element);
    }

    @Override
    public void error(String format, Object... arguments) {
        messager.printError(format.replaceAll("\\{}", "%s").formatted(arguments));
    }

    @Override
    public void errorElement(String format, Element element, Object... arguments) {
        messager.printError(format.replaceAll("\\{}", "%s").formatted(arguments), element);
    }
}
