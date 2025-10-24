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

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.RequiresOP;
import net.strokkur.commands.annotations.arguments.StringArg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import static net.strokkur.commands.StringArgType.GREEDY;

@Command("tellmini")
@RequiresOP
class TellMiniCommand {

  @Executes
  void executes(CommandSender sender, @StringArg(GREEDY) String message) {
    Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<dark_gray>[<b><dark_red>BROADCAST</b>] <red><sender></red> »</dark_gray> <message>",
        Placeholder.component("sender", sender.name()),
        Placeholder.parsed("message", message)
    ));
  }
}