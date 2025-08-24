package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.internal.intermediate.paths.LiteralCommandPath;

import javax.lang.model.element.TypeElement;

public interface CommandParser {
    LiteralCommandPath parseElement(TypeElement typeElement);
}
