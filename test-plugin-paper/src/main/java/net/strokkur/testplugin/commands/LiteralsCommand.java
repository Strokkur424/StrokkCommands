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

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Literal;
import org.bukkit.command.CommandSender;

@Command("literals")
class LiteralsCommand {

  @Executes("hey there, how")
  void executes(CommandSender sender,
                @Literal({"are", "am"}) String first,
                @Literal({"you", "I"}) String second,
                @Literal("doing?") String $doing) {

    if (first.equals("are") && second.equals("you")) {
      sender.sendMessage("I am doing great, thanks for asking :)");
    } else if (first.equals("am") && second.equals("I")) {
      sender.sendMessage("I hope you are doing good?");
    } else {
      sender.sendRichMessage("<red>Dude, what does that even mean");
    }
  }
}
