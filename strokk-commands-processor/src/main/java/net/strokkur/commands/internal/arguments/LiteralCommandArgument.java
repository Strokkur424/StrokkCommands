package net.strokkur.commands.internal.arguments;

public interface LiteralCommandArgument extends CommandArgument {

    String getLiteral();

    @Override
    default String getName() {
        return getLiteral();
    }
}
