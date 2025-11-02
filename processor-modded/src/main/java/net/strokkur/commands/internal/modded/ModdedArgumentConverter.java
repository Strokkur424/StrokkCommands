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
package net.strokkur.commands.internal.modded;

import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.exceptions.ConversionException;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.modded.arguments.AngleArg;
import net.strokkur.commands.modded.arguments.HexColorArg;
import net.strokkur.commands.modded.arguments.MessageArg;
import net.strokkur.commands.modded.arguments.SlotArg;
import net.strokkur.commands.modded.arguments.TimeArg;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class ModdedArgumentConverter extends BrigadierArgumentConverter {
  public ModdedArgumentConverter(final MessagerWrapper messagerWrapper) {
    super(messagerWrapper);
  }

  /// Known issues:
  /// - None of the Resource* arguments are supported
  @Override
  protected void initializeArguments() {
    super.initializeArguments();

    putStandardRoot("ColorArgument", "net.minecraft.ChatFormatting");
    putStandardRoot("ComponentArgument", "textComponent", "getRawComponent", "net.minecraft.network.chat.Component", true);
    putStandardRoot("CompoundTagArgument", "net.minecraft.nbt.CompoundTag");
    putStandardRoot("DimensionArgument", "net.minecraft.server.level.ServerLevel");
    putStandardRoot("EntityAnchorArgument", "Anchor", "net.minecraft.commands.arguments.EntityAnchorArgument.Anchor");

    putStandardRoot("EntityArgument", "net.minecraft.world.entity.Entity");
    putStandardRoot("EntityArgument", "entities", "getEntities", "java.util.Collection<? extends net.minecraft.world.entity.Entity>");
    putStandardRoot("GameModeArgument", "net.minecraft.world.level.GameType");
    putStandardRoot("GameProfileArgument", "gameProfile", "getGameProfiles", "java.util.Collection<net.minecraft.server.players.NameAndId>");
    putStandardRoot("HeightmapTypeArgument", "Heightmap", "net.minecraft.world.level.levelgen.Heightmap.Types");

    putStandardRoot("NbtPathArgument", "nbtPath", "getPath", "net.minecraft.commands.arguments.NbtPathArgument.NbtPath");
    putStandardRoot("NbtTagArgument", "net.minecraft.nbt.Tag");
    putStandardRoot("ObjectiveArgument", "net.minecraft.world.scores.Objective");
    putStandardRoot("ObjectiveCriteriaArgument", "Criteria", "net.minecraft.world.scores.criteria.ObjectiveCriteria");
    putStandardRoot("OperationArgument", "net.minecraft.commands.arguments.OperationArgument.Operation");
    putStandardRoot("ParticleArgument", "net.minecraft.core.particles.ParticleOptions", true);

    putFor((unused, name) -> BrigadierArgumentType.of(
        "RangeArgument.intRange()",
        "RangeArgument.Ints.getRange(ctx, \"%s\")".formatted(name),
        "net.minecraft.commands.arguments.RangeArgument"
    ), "net.minecraft.advancements.critereon.MinMaxBounds.Ints");
    putFor((unused, name) -> BrigadierArgumentType.of(
        "RangeArgument.floatRange()",
        "RangeArgument.Floats.getRange(ctx, \"%s\")".formatted(name),
        "net.minecraft.commands.arguments.RangeArgument"
    ), "net.minecraft.advancements.critereon.MinMaxBounds.Doubles");

    putStandardRoot("ScoreboardSlotArgument", "DisplaySlot", "net.minecraft.world.scores.DisplaySlot");
    putStandardRoot("ScoreHolderArgument", "scoreHolder", "getName", "net.minecraft.world.scores.ScoreHolder");
    putStandardRoot("ScoreHolderArgument", "scoreHolders", "getNames", "java.util.Collection<net.minecraft.world.scores.ScoreHolder>");

    putStandardRoot("SlotsArgument", "net.minecraft.world.inventory.SlotRange");
    putStandardRoot("StyleArgument", "net.minecraft.network.chat.Style", true);
    putStandardRoot("TeamArgument", "net.minecraft.world.scores.PlayerTeam");
    putStandardRoot("TemplateMirrorArgument", "templateMirror", "getMirror", "net.minecraft.world.level.block.Mirror");
    putStandardRoot("TemplateRotationArgument", "templateRotation", "getRotation", "net.minecraft.world.level.block.Rotation");
    putStandardRoot("UuidArgument", "java.util.UUID");

    putStandard(".blocks", "BlockPredicateArgument", "java.util.function.Predicate<net.minecraft.world.level.block.state.pattern.BlockInWorld>", true);
    putStandard(".blocks", "BlockStateArgument", "Block", "net.minecraft.commands.arguments.blocks.BlockInput", true);

    putStandard(".coordinates", "BlockPosArgument", "net.minecraft.core.BlockPos");
    putStandard(".coordinates", "ColumnPosArgument", "net.minecraft.server.level.ColumnPos");
    putStandard(".coordinates", "RotationArgument", "net.minecraft.commands.arguments.coordinates.Coordinates");
    putStandard(".coordinates", "SwizzleArgument", "java.util.Set<net.minecraft.core.Direction.Axis>");
    putStandard(".coordinates", "Vec2Argument", "net.minecraft.world.phys.Vec2");
    putStandard(".coordinates", "Vec3Argument", "net.minecraft.world.phys.Vec3");

    putStandard(".item", "ItemArgument", "net.minecraft.commands.arguments.item.ItemInput", true);
    putStandard(".item", "ItemPredicateArgument", "net.minecraft.commands.arguments.item.ItemPredicateArgument.Result", true);
  }

  //<editor-fold desc="Standard Common">
  private void putStandard(final String subPackage, final String argType, final String key) {
    putStandard(subPackage, argType, key, false);
  }

  private void putStandard(final String subPackage, final String argType, final String argName, final String key) {
    putStandard(subPackage, argType, argName, key, false);

  }

  private void putStandard(final String subPackage, final String argType, final String init, final String getter, final String key) {
    putStandard(subPackage, argType, init, getter, key, false);

  }

  private void putStandard(final String subPackage, final String argType, final String key, final boolean context) {
    final String argName = argType.substring(0, argType.length() - "Argument".length());
    putStandard(subPackage, argType, argName, key, context);
  }

  private void putStandard(final String subPackage, final String argType, final String argName, final String key, final boolean context) {
    putStandard(subPackage, argType, Character.toLowerCase(argName.charAt(0)) + argName.substring(1), "get" + argName, key, context);
  }

  private void putStandard(final String subPackage, final String argType, final String init, final String getter, final String key, final boolean context) {
    putFor((unused, name) -> BrigadierArgumentType.of(
        "%s.%s(%s)".formatted(argType, init, context ? "registryAccess" : ""),
        "%s.%s(ctx, \"%s\")".formatted(argType, getter, name),
        "net.minecraft.commands.arguments" + subPackage + "." + argType
    ), key);
  }
  //</editor-fold>

  //<editor-fold desc="Standard Root">
  private void putStandardRoot(final String argType, final String key) {
    putStandard("", argType, key);
  }

  private void putStandardRoot(final String argType, final String argName, final String key) {
    putStandard("", argType, argName, key);
  }

  private void putStandardRoot(final String argType, final String init, final String getter, final String key) {
    putStandard("", argType, init, getter, key);
  }

  private void putStandardRoot(final String argType, final String key, final boolean context) {
    putStandard("", argType, key, context);
  }

  private void putStandardRoot(final String argType, final String argName, final String key, final boolean context) {
    putStandard("", argType, argName, key, context);
  }

  private void putStandardRoot(final String argType, final String init, final String getter, final String key, final boolean context) {
    putStandard("", argType, init, getter, key, context);
  }
  //</editor-fold>

  @Override
  protected @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(final String argumentName, final String type, final SourceVariable parameter)
      throws ConversionException {
    final AngleArg angleArt = parameter.getAnnotation(AngleArg.class);
    if (angleArt != null) {
      if (!type.equals("float") && !type.equals("java.lang.Float")) {
        throw new ConversionException("An argument annotated with @AngleArg has to be of type 'float'");
      }

      return BrigadierArgumentType.of(
          "AngleArgument.angle()",
          "AngleArgument.getAngle(ctx, \"%s\")".formatted(argumentName),
          "net.minecraft.commands.arguments.AngleArgument"
      );
    }

    final HexColorArg hexColorArg = parameter.getAnnotation(HexColorArg.class);
    if (hexColorArg != null) {
      if (!type.equals("int") && !type.equals("java.lang.Integer")) {
        throw new ConversionException("An argument annotated with @HexColorArg has to be of type 'Integer'");
      }

      return BrigadierArgumentType.of(
          "HexColorArgument.hexColor()",
          "HexColorArgument.getHexColor(ctx, \"%s\")".formatted(argumentName),
          "net.minecraft.commands.arguments.HexColorArgument"
      );
    }

    final MessageArg messageArg = parameter.getAnnotation(MessageArg.class);
    if (messageArg != null) {
      if (!type.equals("net.minecraft.network.chat.Component")) {
        throw new ConversionException("An argument annotated with @MessageArg has to be of type 'net.minecraft.network.chat.Component'");
      }

      return BrigadierArgumentType.of(
          "MessageArgument.message()",
          "MessageArgument.getMessage(ctx, \"%s\")".formatted(argumentName),
          "net.minecraft.commands.arguments.MessageArgument"
      );
    }

    final SlotArg slotArg = parameter.getAnnotation(SlotArg.class);
    if (slotArg != null) {
      if (!(type.equals("int") || type.equals("java.lang.Integer"))) {
        throw new ConversionException("An argument annotated with @SlotArg has to be of type 'int'");
      }

      return BrigadierArgumentType.of(
          "SlotArgument.slot()",
          "SlotArgument.getSlot(ctx, \"%s\")".formatted(argumentName),
          "net.minecraft.commands.arguments.SlotArgument"
      );
    }

    final TimeArg timeArg = parameter.getAnnotation(TimeArg.class);
    if (timeArg != null) {
      if (!(type.equals("int") || type.equals("java.lang.Integer"))) {
        throw new ConversionException("An argument annotated with @TimeArg has to be of type 'int'");
      }

      return BrigadierArgumentType.of(
          "TimeArgument.time(%s)".formatted(timeArg.value() == 0 ? "" : Integer.toString(timeArg.value())),
          "IntegerArgumentType.getInteger(ctx, \"%s\")".formatted(argumentName),
          Set.of(
              "net.minecraft.commands.arguments.TimeArgument",
              "com.mojang.brigadier.arguments.IntegerArgumentType"
          )
      );
    }

    return null;
  }
}
