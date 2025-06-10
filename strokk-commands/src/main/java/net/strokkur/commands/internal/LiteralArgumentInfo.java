package net.strokkur.commands.internal;

public interface LiteralArgumentInfo extends ArgumentInformation {
    String getLiteral();
    boolean addToMethod();
    
    LiteralArgumentInfo withLiteral(String literal);
}