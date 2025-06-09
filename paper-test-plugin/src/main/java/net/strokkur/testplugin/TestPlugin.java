package net.strokkur.testplugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.strokkur.testplugin.commands.SimpleCommandBrigadier;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            SimpleCommandBrigadier.register(event.registrar());
        }));
    }

    @Override
    public void onEnable() {

    }
}
