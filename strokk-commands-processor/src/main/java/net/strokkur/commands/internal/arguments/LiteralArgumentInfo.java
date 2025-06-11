package net.strokkur.commands.internal.arguments;

public interface LiteralArgumentInfo extends ArgumentInformation {
    String getLiteral();

    boolean addToMethod();

    LiteralArgumentInfo withLiteral(String literal);
}