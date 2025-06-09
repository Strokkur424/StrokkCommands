package net.strokkur.testplugin;

import net.strokkur.testplugin.commands.SimpleCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        new SimpleCommand();
//        new CommandsManager(this).register(SimpleCommand.class);
    }

    @Override
    public void onEnable() {
        
    }
}
