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

import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Literal;
import net.strokkur.commands.annotations.arguments.StringArg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

import static net.strokkur.commands.StringArgType.STRING;

//@Command("adventure")
class AdventureArgumentsCommand {

    @Executes("send message")
    void executes(CommandSender sender, @StringArg(STRING) String message, @Literal("with") String $with, @Literal("color") String $color, NamedTextColor color) {
        Bukkit.broadcast(Component.text(message, color));
    }

    @Executes("send signed message")
    void executes(CommandSender sender, @Executor Player player, CompletableFuture<SignedMessage> message) {
        message.thenAccept(msg -> Bukkit.getServer().sendMessage(msg, ChatType.CHAT.bind(player.name())));
    }
}
