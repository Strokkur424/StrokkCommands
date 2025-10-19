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
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.DefaultExecutes;
import net.strokkur.commands.annotations.Executes;
import org.bukkit.command.CommandSender;

import java.util.List;

@Command("command-with-help")
class One {

  @Executes("a lot of literals so yeah")
  void execute(CommandSender sender, int num, String stringArg) {
    sender.sendPlainMessage("The normal executes was executed!");
  }

  @Executes("another lot of literals")
  void execute(CommandSender sender) {
    sender.sendPlainMessage("Another normal executes was executed!");
  }

  @DefaultExecutes
  void defaultExecute(CommandSender sender, List<String> args) {
    args.addFirst("/command-with-help");
    final String cmd = String.join(" ", args);

    sender.sendRichMessage("""
            <gray>The command <aqua><cmd></aqua> is <red><b>incomplete<red>!

            The following commands are valid:
             <b><white>*</b> <c:#4556ff>/command-with-help a lot of literals so yeah <number> <string></c>
             <b><white>*</b> <c:#4556ff>/command-with-help another lot of literals</c>

            Thank you for choosing StrokkCommands.""",
        Placeholder.parsed("cmd", cmd)
    );
  }
}

@Command("two")
class Two {
  @Executes("hehe")
  void executes(CommandSender sender, int number, String str) {
    //
  }

  @DefaultExecutes("hehe")
  void executeshelp(CommandSender sender, int number) {
    // ..
  }
}
