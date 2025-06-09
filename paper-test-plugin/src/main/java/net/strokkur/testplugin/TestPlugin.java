package net.strokkur.testplugin;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.strokkur.testplugin.commands.EntitiesCommandBrigadier;
import net.strokkur.testplugin.commands.LocationArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.PaperArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.PredicateArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.PrimitivesCommandBrigadier;
import net.strokkur.testplugin.commands.RegistryArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.TellMiniCommandBrigadier;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public final class TestPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            Commands commands = event.registrar();
            PrimitivesCommandBrigadier.register(commands);
            LocationArgumentsCommandBrigadier.register(commands);
            EntitiesCommandBrigadier.register(commands);
            RegistryArgumentsCommandBrigadier.register(commands);
            PaperArgumentsCommandBrigadier.register(commands);
            PredicateArgumentsCommandBrigadier.register(commands);
            TellMiniCommandBrigadier.register(commands);
        }));
    }
}