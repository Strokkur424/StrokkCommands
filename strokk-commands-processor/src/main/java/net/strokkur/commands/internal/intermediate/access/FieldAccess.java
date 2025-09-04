package net.strokkur.commands.internal.intermediate.access;

import javax.lang.model.element.VariableElement;

public interface FieldAccess extends ExecuteAccess {
    VariableElement field();
}
