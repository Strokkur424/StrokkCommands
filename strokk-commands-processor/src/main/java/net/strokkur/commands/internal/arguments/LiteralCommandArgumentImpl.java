package net.strokkur.commands.internal.arguments;

import javax.lang.model.element.Element;

record LiteralCommandArgumentImpl(String literal, Element element) implements LiteralCommandArgument {}
