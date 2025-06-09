package net.strokkur.commands.tree;

import net.strokkur.commands.objects.ArgumentInformation;
import net.strokkur.commands.objects.ExecutorInformation;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface CommandArgument {

    void put(CommandArgument argument);
    
    String getName();

    @Nullable
    List<CommandArgument> getSubArguments();

    @Nullable
    ExecutorInformation getExecutor();

    ArgumentInformation getArgumentInformation();
}
