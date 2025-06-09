package net.strokkur.commands.tree;

import net.strokkur.commands.objects.ArgumentInformation;
import net.strokkur.commands.objects.ExecutorInformation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandArgumentImpl implements CommandArgument {

    private String name;
    private ArgumentInformation argumentInformation;
    private List<CommandArgument> subArguments;
    private @Nullable ExecutorInformation executor;

    public CommandArgumentImpl(String name, ArgumentInformation argumentInformation, @Nullable ExecutorInformation executor) {
        this.name = name;
        this.argumentInformation = argumentInformation;
        this.subArguments = new ArrayList<>();
        this.executor = executor;
    }

    @Override
    public void put(CommandArgument argument) {
        subArguments.add(argument);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable List<CommandArgument> getSubArguments() {
        return subArguments;
    }

    @Override
    public @Nullable ExecutorInformation getExecutor() {
        return executor;
    }

    public ArgumentInformation getArgumentInformation() {
        return argumentInformation;
    }
}
