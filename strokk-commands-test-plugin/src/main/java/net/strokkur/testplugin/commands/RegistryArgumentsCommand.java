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
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.ItemType;

@SuppressWarnings("UnstableApiUsage")
@Command("registryarg")
class RegistryArgumentsCommand {

    @Executes("itemtype")
    void execute(CommandSender sender, @Executor Player player, ItemType type) {
        execute(sender, player, type, 1);
    }

    @Executes("itemtype")
    void execute(CommandSender sender, @Executor Player player, ItemType type, int amount) {
        player.give(type.createItemStack(amount));
        sender.sendRichMessage("<green>Successfully gave <white><player></white> <red><amount>x <type></red>",
            Placeholder.component("player", player.name()),
            Placeholder.unparsed("amount", Integer.toString(amount)),
            Placeholder.component("type", Component.translatable(type))
        );
    }

    @Executes("cow-variant")
    void execute(CommandSender sender, Cow.Variant cowVariant) {
        sender.sendRichMessage("<green>You selected <white><variant></white>!",
            Placeholder.unparsed("variant", cowVariant.key().asString())
        );
    }

    @Executes("memory-key")
    void execute(CommandSender sender, MemoryKey<?> memoryKey) {
        sender.sendRichMessage("<green>You selected <white><variant></white>!",
            Placeholder.unparsed("variant", memoryKey.key().asString())
        );
    }

    @SuppressWarnings("removal")
    @Executes("play-sound")
    void execute(CommandSender sender, Sound sound) {
        sender.playSound(net.kyori.adventure.sound.Sound.sound(sound, net.kyori.adventure.sound.Sound.Source.AMBIENT, 1.0f, 1.0f));
        sender.sendRichMessage("<aqua>Played <white><sound>",
            Placeholder.unparsed("sound", sound.key().asString()) // <-- I don't care about the deprecation, shush
        );
    }
}
