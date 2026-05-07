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
package net.strokkur.testplugin.defaulthelp;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import org.bukkit.command.CommandSender;

@Command("static-subcommands-with-help")
class StaticSubcommandsWithHelp {

  @DefaultExecutes
  void printHelp(CommandSender sender, String[] args) {
    sender.sendRichMessage("/<cmd> - Root command", Placeholder.unparsed("cmd", args[0]));
    sender.sendRichMessage("/<cmd> subcommand <int> - Sub command", Placeholder.unparsed("cmd", args[0]));
  }

  @Subcommand("subcommand")
  static class Sub {
    @Executes
    void runInt(CommandSender sender, int val) {
      sender.sendRichMessage("Value: <red>%s".formatted(val));
    }
  }
}
