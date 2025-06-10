package net.strokkur.commands.internal;

public interface LiteralArgumentInfo extends ArgumentInformation {
    String literal();
    boolean addToMethod();
    
    LiteralArgumentInfo withLiteral(String literal);
}