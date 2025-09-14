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
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Subcommand;
import org.bukkit.command.CommandSender;

@Command("nestedfields")
class NestedFields {

  static {
    // Expectation
    final NestedFields instance = new NestedFields();
    instance.firstNesting = new FirstNesting();
    instance.firstNesting.secondNesting = new SecondNesting();

    var built = Commands.literal("nestedfields")
        .then(Commands.literal("first")
            .then(Commands.literal("second")
                .executes(ctx -> {
                  instance.firstNesting.secondNesting.execute(
                      ctx.getSource().getSender()
                  );
                  return 1;
                })
            )
        )
        .build();
  }

  @Subcommand("first")
  FirstNesting firstNesting;

  static class FirstNesting {

    @Subcommand("second")
    SecondNesting secondNesting;
  }

  static class SecondNesting {

    @Executes
    void execute(CommandSender sender) {
      sender.sendMessage("Wohoo");
    }
  }
}
