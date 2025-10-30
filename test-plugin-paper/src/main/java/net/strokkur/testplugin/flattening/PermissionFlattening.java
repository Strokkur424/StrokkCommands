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
import net.strokkur.commands.paper.Permission;
import org.bukkit.command.CommandSender;

@Command("permission-flattening")
class PermissionFlattening {

  @Subcommand("first")
  static class First {
    @Executes("one")
    @Permission("first.one")
    void one(CommandSender sender, String top) {
    }

    @Executes("anotherone")
    @Permission("first.one")
    void anotherOne(CommandSender sender, String top) {
    }

    @Executes("two")
    @Permission("first.two")
    void two(CommandSender sender, String top) {
    }
  }

  @Permission("second.perm")
  void second(CommandSender sender, String top, @Literal String second) {
  }

  @Subcommand("third")
  static class Third {
    @Executes("one")
    @Permission("third.one")
    void one(CommandSender sender, String top) {
    }

    @Executes("anotherone")
    @Permission("third.one")
    void anotherOne(CommandSender sender, String top) {

    }
  }
}
