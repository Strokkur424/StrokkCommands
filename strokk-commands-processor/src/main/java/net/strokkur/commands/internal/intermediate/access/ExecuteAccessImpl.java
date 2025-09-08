package net.strokkur.commands.internal.intermediate.access;

import javax.lang.model.element.Element;

abstract class ExecuteAccessImpl<E extends Element> implements ExecuteAccess<E> {

    protected final E element;

    public ExecuteAccessImpl(final E element) {
        this.element = element;
    }

    @Override
    public E getElement() {
        return this.element;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
            "element=" + this.getElement().getSimpleName() +
            '}';
    }
}
