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
package net.strokkur.testmod.fabric.server.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.strokkur.commands.Command;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.modded.arguments.AngleArg;
import net.strokkur.commands.modded.arguments.HexColorArg;
import net.strokkur.commands.modded.arguments.MessageArg;
import net.strokkur.commands.modded.arguments.SlotArg;
import net.strokkur.commands.modded.arguments.TimeArg;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Command("monstrous")
class MonsterCommand {
  @DefaultExecutes
  void defaultExecutes(CommandSourceStack source, List<String> input) {
    source.sendSuccess(() -> Component.literal("You entered: /" + String.join(" ", input)), false);
  }

  @Executes("uh oh")
  void execute(
      final CommandSourceStack source,
      final ChatFormatting chatformat,
      final Component textcomponent,
      final CompoundTag compoundTag,
      final ServerLevel level,
      final EntityAnchorArgument.Anchor anchor,
      final Entity entity,
      final Collection<? extends Entity> multipleEntities,
      final GameType type,
      final Collection<NameAndId> profiles,
      final Heightmap.Types heightmapTypes,
      final NbtPathArgument.NbtPath nbtPath,
      final Tag tag,
      final Objective objective,
      final ObjectiveCriteria criteria,
      final OperationArgument.Operation operation,
      final ParticleOptions particle,
      final MinMaxBounds.Ints intRange,
      final MinMaxBounds.Doubles doubleRange,
      final DisplaySlot slot,
      final ScoreHolder holder,
      final Collection<ScoreHolder> holders,
      final SlotRange range,
      final Style style,
      final PlayerTeam team,
      final Mirror templateMirror,
      final Rotation templateRotation,
      final UUID uuid,
      final Predicate<BlockInWorld> blockPredicate,
      final BlockInput block,
      final BlockPos blockPos,
      final ColumnPos columnPos,
      final Coordinates rotation,
      final Set<Direction.Axis> swizzle,
      final Vec2 vec2,
      final Vec3 vec3,
      final ItemInput item,
      final ItemPredicateArgument.Result itemPredicate,
      final @AngleArg float angle,
      final @HexColorArg int aColor,
      final @SlotArg int someSlot,
      final @TimeArg int time,
      final @MessageArg Component someMessage
  ) {
    source.sendSuccess(() -> Component.literal("This was a mistake."), true);
  }
}
