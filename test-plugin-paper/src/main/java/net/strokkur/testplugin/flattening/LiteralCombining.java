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
package net.strokkur.testplugin.flattening;

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Literal;
import net.strokkur.commands.Subcommand;
import org.bukkit.command.CommandSender;

@Command("combined")
class LiteralCombining {

  @Executes("sub")
  void normalSub(CommandSender sender) {
    sender.sendRichMessage("<gold>Normal executes method.");
  }

  @Executes
  void literalSub(CommandSender sender, @Literal String sub, @Literal String two) {
    sender.sendRichMessage("<gold>Literal executes method.");
  }

  @Subcommand("sub class")
  static class SubClass {

    @Executes
    void execute(CommandSender sender) {
      sender.sendRichMessage("<gold>Sub class executes method.");
    }
  }
}
