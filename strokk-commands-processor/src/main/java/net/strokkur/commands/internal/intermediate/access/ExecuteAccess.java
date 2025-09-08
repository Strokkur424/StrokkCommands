package net.strokkur.commands.internal.intermediate.access;

import javax.lang.model.element.Element;

public interface ExecuteAccess<E extends Element> {

    E getElement();
}
