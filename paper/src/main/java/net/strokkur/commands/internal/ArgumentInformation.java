package net.strokkur.commands.internal;

import javax.lang.model.element.Element;

interface ArgumentInformation {
    String argumentName();

    Element element();
}
