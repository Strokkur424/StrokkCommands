package net.strokkur.commands.internal.arguments;

import javax.lang.model.element.Element;

public interface LiteralCommandArgument extends CommandArgument {

    static LiteralCommandArgument literal(String literal, Element element) {
        return new LiteralCommandArgumentImpl(literal, element);
    }

    String literal();

    @Override
    default String getName() {
        return literal();
    }
}
