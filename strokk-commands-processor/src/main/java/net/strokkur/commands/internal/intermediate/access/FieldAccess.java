package net.strokkur.commands.internal.intermediate.access;

import net.strokkur.commands.internal.util.Utils;

import javax.lang.model.element.VariableElement;

public interface FieldAccess extends ExecuteAccess<VariableElement> {

    @Override
    default String getTypeName() {
        return Utils.getTypeName(getElement().asType());
    }
}
