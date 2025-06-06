package net.strokkur.commands;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.strokkur.commands.exceptions.NoNoArgsConstructorFound;
import net.strokkur.commands.objects.CommandInformation;
import net.strokkur.commands.objects.ExecutorInformation;
import net.strokkur.commands.reflection.ReflectionHelper;
import net.strokkur.commands.utils.BrigadierUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
public class CommandsManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsManager.class);
    
    private final JavaPlugin plugin;

    public CommandsManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(Class<?> commandClass) {
        LOGGER.info("Registering {}:", commandClass.getName());
        CommandInformation command = ReflectionHelper.getInformation(commandClass);
        List<ExecutorInformation> executors = ReflectionHelper.getCommandExecutors(commandClass);

        Object instance;
        try {
            instance = commandClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new NoNoArgsConstructorFound(commandClass, exception);
        }

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            event.registrar().register(BrigadierUtils.buildTree(instance, command, executors), command.description(),
                command.aliases() == null ? List.of() : List.of(command.aliases())
            );
        }));
    }
}
