package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.internal.intermediate.paths.CommandPath;

import javax.lang.model.element.Element;

interface PathTransform {

    void transform(CommandPath<?> parent, Element element);

    boolean canTransform(Element element);
}
