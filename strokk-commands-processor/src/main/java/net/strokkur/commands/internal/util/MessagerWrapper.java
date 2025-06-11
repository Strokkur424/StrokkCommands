package net.strokkur.commands.internal.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

public interface MessagerWrapper {

    static MessagerWrapper wrap(Messager messager) {
        return new MessagerWrapperImpl(messager);
    }

    void info(String format, Object... arguments);

    void infoElement(String format, Element element, Object... arguments);

    void error(String format, Object... arguments);

    void errorElement(String format, Element element, Object... arguments);
}
