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

@Command("supernested")
class SuperNested {

  static {
    // Expectation
    final NestedClass instanceInnerNonStaticNested = new NestedClass();
    final NestedClass.UltraNested instanceInnerNonStaticNestedUltraNested = instanceInnerNonStaticNested.new UltraNested();

    var built = Commands.literal("supernested")
        .then(Commands.literal("nonstatic")
            .then(Commands.literal("ULTRA-NESTED")
                .executes(ctx -> {
                  instanceInnerNonStaticNestedUltraNested.execute(
                      ctx.getSource().getSender()
                  );
                  return 1;
                })
            )
        )
        .build();
  }

  static class NestedClass {

    @Subcommand("ULTRA-NESTED")
    class UltraNested {

      @Executes
      void execute(CommandSender sender) {
        sender.sendRichMessage("<rainbow>SUPER ULTRA NESTED CLASS WOWOOOOWOWWOWOW");
      }
    }
  }

  @Subcommand("nonstatic")
  class InnerNonStatic {

    @Subcommand
    NestedClass nested;
  }
}
