package net.strokkur.commands.internal.arguments;

import javax.lang.model.element.Element;

public interface CommandArgument {

    String getName();

    Element element();
}
