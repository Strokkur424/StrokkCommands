package net.strokkur.commands.internal;

import java.util.List;

interface MultiLiteralsTree {

    void insert(ArgumentInformation single);

    void insert(LiteralArgumentInfo base, List<String> literals);

    List<List<ArgumentInformation>> flatten();

    static MultiLiteralsTree create() {
        return new MultiLiteralsTreeImpl();
    }
}
