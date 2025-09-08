package net.strokkur.commands.internal.intermediate.access;

import net.strokkur.commands.internal.util.Utils;

import javax.lang.model.element.VariableElement;

class FieldAccessImpl extends ExecuteAccessImpl<VariableElement> implements FieldAccess {

    public FieldAccessImpl(final VariableElement element) {
        super(element);
    }

    @Override
    public String toString() {
        return "FieldAccessImpl{" +
            "element=" + element + ',' +
            "initialized=" + Utils.isFieldInitialized(element) +
            '}';
    }
}
