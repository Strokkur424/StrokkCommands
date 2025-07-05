/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.testplugin;

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
import net.strokkur.testplugin.docs.MyFirstCommandBrigadier;
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

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            MyFirstCommandBrigadier.register(event.registrar());
        }));
    }
}