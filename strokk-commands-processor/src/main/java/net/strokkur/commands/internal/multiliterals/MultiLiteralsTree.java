package net.strokkur.commands.internal.multiliterals;

import net.strokkur.commands.internal.arguments.ArgumentInformation;
import net.strokkur.commands.internal.arguments.LiteralArgumentInfo;

import java.util.List;

public interface MultiLiteralsTree {

    static MultiLiteralsTree create() {
        return new MultiLiteralsTreeImpl();
    }

    void insert(ArgumentInformation single);

    void insert(LiteralArgumentInfo base, List<String> literals);

    List<List<ArgumentInformation>> flatten();
}
