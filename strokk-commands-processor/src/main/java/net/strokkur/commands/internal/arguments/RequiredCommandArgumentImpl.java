package net.strokkur.commands.internal.arguments;

import javax.lang.model.element.Element;

public class RequiredCommandArgumentImpl implements RequiredCommandArgument {

    private final BrigadierArgumentType argumentType;
    private final String name;
    private final Element element;

    public RequiredCommandArgumentImpl(final BrigadierArgumentType argumentType, final String name, final Element element) {
        this.argumentType = argumentType;
        this.name = name;
        this.element = element;
    }

    @Override
    public BrigadierArgumentType getArgumentType() {
        return argumentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Element element() {
        return element;
    }
}
