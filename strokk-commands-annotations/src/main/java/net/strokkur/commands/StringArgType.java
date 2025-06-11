package net.strokkur.commands;

public enum StringArgType {
    WORD("word"),
    STRING("string"),
    GREEDY("greedyString");
    
    private final String brigadierType;

    StringArgType(String brigadierType) {
        this.brigadierType = brigadierType;
    }

    public String getBrigadierType() {
        return brigadierType;
    }
}
