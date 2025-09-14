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
package net.strokkur.testplugin.docs;

import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnstableApiUsage")
@Command("fillblock")
record FillBlockCommand(BlockPosition pos1, BlockPosition pos2, BlockState state) {

  @Executes
  void execute(CommandSender sender) {
    execute(sender, 1000);
  }

  @Executes
  void execute(CommandSender sender, int perTick) {
    sender.sendRichMessage("You attempted to fill all blocks between <pos1> and <pos2> with <type> (placing <per_tick> blocks per tick.)",
        Placeholder.unparsed("pos1", "%d %d %d".formatted(pos1.blockX(), pos1.blockY(), pos1.blockZ())),
        Placeholder.unparsed("pos2", "%d %d %d".formatted(pos2.blockX(), pos2.blockY(), pos2.blockZ())),
        Placeholder.component("type", Component.translatable(state.getType())),
        Placeholder.component("per_tick", Component.text(perTick))
    );
  }
}