package net.strokkur.commands.internal.intermediate.access;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public interface ExecuteAccess<E extends Element> {

    E getElement();

    String getTypeName();

    static FieldAccess of(VariableElement fieldElement) {
        return new FieldAccessImpl(fieldElement);
    }

    static InstanceAccess of(TypeElement typeElement) {
        return new InstanceAccessImpl(typeElement);
    }
}
