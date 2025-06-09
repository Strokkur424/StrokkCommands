package net.strokkur.commands.internal;

record BrigadierArgumentType(String initializer, String retriever) {

    public static BrigadierArgumentType of(String initializer, String retriever) {
        return new BrigadierArgumentType(initializer, retriever);
    }
}
