package net.strokkur.testplugin;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.strokkur.testplugin.commands.LocationArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.PrimitivesCommandBrigadier;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public final class TestPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            Commands commands = event.registrar();
            PrimitivesCommandBrigadier.register(commands);
            LocationArgumentsCommandBrigadier.register(commands);
//            SimpleCommandBrigadier.register(commands);
//            TellMiniCommandBrigadier.register(commands);
        }));
    }
}