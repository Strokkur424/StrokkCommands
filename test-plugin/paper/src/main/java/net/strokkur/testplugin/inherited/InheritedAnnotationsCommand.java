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
package net.strokkur.testplugin.inherited;

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import org.bukkit.command.CommandSender;

@Executes("help")
@interface HelpExecutes {}

@Subcommand("admin")
@interface AdminSubcommand {}

@Executes("info")
@interface InfoExecutes {}

@Command("inheritedtest")
class InheritedAnnotationsCommand {

  @Executes("regular")
  void regularCommand(CommandSender sender) {
    sender.sendRichMessage("<green>Regular @Executes command!");
  }

  @HelpExecutes
  void helpCommand(CommandSender sender) {
    sender.sendRichMessage("<aqua>Help command using inherited annotation!");
    sender.sendRichMessage("<gray>Commands:");
    sender.sendRichMessage("<gray>- /inheritedtest regular");
    sender.sendRichMessage("<gray>- /inheritedtest help");
    sender.sendRichMessage("<gray>- /inheritedtest info");
    sender.sendRichMessage("<gray>- /inheritedtest admin ...");
  }

  @InfoExecutes
  void infoCommand(CommandSender sender) {
    sender.sendRichMessage("<yellow>Info command using inherited annotation!");
  }

  @AdminSubcommand
  static class AdminCommands {

    @Executes("status")
    void status(CommandSender sender) {
      sender.sendRichMessage("<gold>Admin status: All systems operational!");
    }

    @Executes("reload")
    void reload(CommandSender sender) {
      sender.sendRichMessage("<gold>Configuration reloaded!");
    }
  }
}
