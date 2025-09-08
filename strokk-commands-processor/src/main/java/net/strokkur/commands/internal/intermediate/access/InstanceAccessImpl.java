package net.strokkur.commands.internal.intermediate.access;

import javax.lang.model.element.TypeElement;

class InstanceAccessImpl extends ExecuteAccessImpl<TypeElement> implements InstanceAccess {

    public InstanceAccessImpl(final TypeElement element) {
        super(element);
    }
}
