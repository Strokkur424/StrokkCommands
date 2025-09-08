package net.strokkur.commands.internal.intermediate.access;

import net.strokkur.commands.internal.util.Utils;

import javax.lang.model.element.TypeElement;

public interface InstanceAccess extends ExecuteAccess<TypeElement> {

    @Override
    default String getTypeName() {
        return Utils.getTypeName(getElement());
    }
}
