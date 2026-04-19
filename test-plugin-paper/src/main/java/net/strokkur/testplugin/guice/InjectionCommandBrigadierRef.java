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
package net.strokkur.testplugin.guice;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jakarta.inject.Inject;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class InjectionCommandBrigadierRef {
  private @Inject InjectionCommand instance;

  public void register(Commands commands) {
    commands.register(create().build());
  }

  public LiteralArgumentBuilder<CommandSourceStack> create() {
    return Commands.literal("injection-test")
        .executes(ctx -> {
          this.instance.execute(ctx.getSource().getSender());
          return Command.SINGLE_SUCCESS;
        });
  }
}
