package net.strokkur.testplugin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.strokkur.testplugin.commands.AdventureArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.EntitiesCommandBrigadier;
import net.strokkur.testplugin.commands.LiteralsCommandBrigadier;
import net.strokkur.testplugin.commands.LocationArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.PaperArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.PredicateArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.PrimitivesCommandBrigadier;
import net.strokkur.testplugin.commands.RegistryArgumentsCommandBrigadier;
import net.strokkur.testplugin.commands.TellMiniCommandBrigadier;
import net.strokkur.testplugin.iceacream.IceCreamCommandBrigadier;
import net.strokkur.testplugin.suggestions.CommandWithSuggestionsBrigadier;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public final class TestPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            Commands commands = event.registrar();

            CommandWithSuggestionsBrigadier.register(commands);
            IceCreamCommandBrigadier.register(commands);

            PrimitivesCommandBrigadier.register(commands);
            LocationArgumentsCommandBrigadier.register(commands);
            EntitiesCommandBrigadier.register(commands);
            RegistryArgumentsCommandBrigadier.register(commands);
            PaperArgumentsCommandBrigadier.register(commands);
            PredicateArgumentsCommandBrigadier.register(commands);
            AdventureArgumentsCommandBrigadier.register(commands);
            LiteralsCommandBrigadier.register(commands);
            TellMiniCommandBrigadier.register(commands);
        }));
    }
}