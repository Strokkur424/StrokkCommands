package net.strokkur.commands.internal.intermediate.access;

import javax.lang.model.element.TypeElement;

public interface InstanceAccess extends ExecuteAccess {
    TypeElement element();
}
