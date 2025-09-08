package net.strokkur.commands.internal.intermediate.access;

import javax.lang.model.element.VariableElement;

class FieldAccessImpl extends ExecuteAccessImpl<VariableElement> implements FieldAccess {

    public FieldAccessImpl(final VariableElement element) {
        super(element);
    }
}
