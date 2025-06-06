package net.strokkur.commands.objects;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public enum ExecutorType implements Predicate<CommandSourceStack> {
    NONE(stack -> true),
    ENTITY(stack -> stack.getExecutor() != null),
    PLAYER(stack -> stack.getExecutor() instanceof Player);
    
    private final Predicate<CommandSourceStack> predicate;

    ExecutorType(Predicate<CommandSourceStack> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(CommandSourceStack commandSourceStack) {
        return predicate.test(commandSourceStack);
    }
}
