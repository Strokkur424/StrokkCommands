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
package net.strokkur.testplugin.externalsubcommands;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

class InnerCommandNested {

  static {
    // Expectation
    final InnerCommandNested instance = new InnerCommandNested();
    final InnerCommandNested.Nested instanceNested = instance.new Nested();
    final InnerCommandNested.MyNestedClass instanceNestedMyNestedClass = instanceNested.myNestedClass;

    var built = Commands.literal("innercommandnested")
        .then(Commands.literal("nested")
            .executes(ctx -> {
              instanceNestedMyNestedClass.execute(
                  ctx.getSource().getSender()
              );
              return 1;
            })
        )
        .build();
  }

  static class MyNestedClass {

    void execute(CommandSender sender) {
      sender.sendRichMessage("<green>Hi double nested.");
    }
  }

  class Nested {

    MyNestedClass myNestedClass;
  }
}
