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
package net.strokkur.testplugin.optional;

import io.papermc.paper.command.brigadier.argument.AxisSet;
import io.papermc.paper.math.BlockPosition;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.paper.Executor;
import net.strokkur.testplugin.TestPlugin;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

@SuppressWarnings({"UnstableApiUsage", "OptionalUsedAsFieldOrParameterType"})
@Command("optional-mixed")
class MixedOptionalArguments {

  @Executes("fill-line")
  void fill(
    @Executor Player player, BlockType type, int length,
    Optional<BlockPosition> from, @Nullable AxisSet axes,
    OptionalInt tickDelay
  ) {
    final BlockPosition fromPos = from.orElse(player.getLocation().toBlock());
    final Axis axis = axes == null ? Axis.Z : axes.stream().findAny().orElseThrow();
    final int delay = tickDelay.orElse(0);

    final BlockFiller filler = new BlockFiller(axis, delay, length, fromPos, type, player.getWorld());
    filler.nextBlockSet();
  }

  private static final class BlockFiller {
    private final World world;
    private final BlockType type;
    private BlockPosition position;
    private int remainingLength;
    private final int delay;
    private final Axis axis;

    BlockFiller(Axis axis, int delay, int remainingLength, BlockPosition position, BlockType type, World world) {
      this.axis = axis;
      this.delay = delay;
      this.remainingLength = remainingLength;
      this.position = position;
      this.type = type;
      this.world = world;
    }

    public void nextBlockSet() {
      if (remainingLength == 0) {
        return;
      }

      world.setBlockData(position.blockX(), position.blockY(), position.blockZ(), type.createBlockData());
      position = position.offset(axis, 1);
      remainingLength--;

      if (delay == 0) {
        nextBlockSet();
      } else {
        Bukkit.getRegionScheduler().runDelayed(
          TestPlugin.getPlugin(TestPlugin.class),
          world, position.blockX() >> 4, position.blockZ() >> 4,
          ignored -> nextBlockSet(),
          delay
        );
      }
    }
  }
}
