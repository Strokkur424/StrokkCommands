package net.strokkur.commands.internal.util;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public interface ForwardingMessagerWrapper extends MessagerWrapper {

    /**
     * {@return the messager wrapper to delegate all logger calls to}
     */
    MessagerWrapper delegateMessager();

    /**
     * {@inheritDoc}
     */
    @Override
    default void print(Diagnostic.Kind kind, String format, Object... arguments) {
        delegateMessager().print(kind, format, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void printElement(Diagnostic.Kind kind, String format, Element element, Object... arguments) {
        delegateMessager().printElement(kind, format, element, arguments);
    }
}
