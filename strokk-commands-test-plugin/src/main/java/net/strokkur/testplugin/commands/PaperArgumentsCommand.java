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
package net.strokkur.testplugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.RequiresOP;
import net.strokkur.commands.annotations.arguments.TimeArg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command("paperargs")
class PaperArgumentsCommand {

    @Executes("time")
    void executes(CommandSender sender, @Executor Entity entity, @TimeArg int time) {
        entity.getWorld().setTime(entity.getWorld().getTime() + time);
        sender.sendRichMessage("<aqua>Successfully fast-forwarded the world's time by <red><amount> ticks</red>!",
            Placeholder.unparsed("amount", Integer.toString(time))
        );
    }

    @Executes("item")
    void executes(CommandSender sender, @Executor Player player, ItemStack itemStack) {
        player.give(itemStack);
        sender.sendRichMessage("<aqua>Successfully gave <player> a <red><item></red>!",
            Placeholder.component("player", player.displayName()),
            Placeholder.component("item", Component.translatable(itemStack))
        );
    }
}
