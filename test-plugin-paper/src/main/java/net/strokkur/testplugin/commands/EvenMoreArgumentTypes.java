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
package net.strokkur.testplugin.commands;

import io.papermc.paper.command.brigadier.argument.AxisSet;
import io.papermc.paper.command.brigadier.argument.position.ColumnBlockPosition;
import io.papermc.paper.command.brigadier.argument.position.ColumnFinePosition;
import io.papermc.paper.command.brigadier.argument.predicate.BlockInWorldPredicate;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.paper.Executor;
import net.strokkur.commands.paper.arguments.AngleArg;
import org.bukkit.Axis;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
@Command("test-more-argument-types")
class EvenMoreArgumentTypes {

  @Executes("angle")
  void angle(CommandSender sender, @AngleArg float angle) {
    sender.sendRichMessage("You input: <aqua><angle></aqua>!",
        Placeholder.parsed("angle", Float.toString(angle))
    );
  }

  @Executes("swizzle")
  void swizzle(CommandSender sender, AxisSet axes) {
    final StringBuilder builder = new StringBuilder();
    if (axes.contains(Axis.Z)) {
      builder.append("<blue>NORTH-SOUTH ");
    }
    if (axes.contains(Axis.X)) {
      builder.append("<red>WEST-EAST ");
    }
    if (axes.contains(Axis.Y)) {
      builder.append("<green>UP-DOWN");
    }

    sender.sendRichMessage("Axes selected: <directions>",
        Placeholder.parsed("directions", builder.toString().strip())
    );
  }

  @Executes("block-predicate")
  void blockPredicate(CommandSender sender, @Executor Player player, BlockPosition toTest, BlockInWorldPredicate predicate) {
    sender.sendRichMessage(switch (predicate.testBlock(toTest.toLocation(player.getWorld()).getBlock())) {
      case TRUE -> "The block at <loc> <green>matches with your predicate</green>!";
      case FALSE -> "The block at <loc> <red>does not match with your predicate</red>!";
      default -> "The block at <loc> is not loaded!";
    }, Placeholder.parsed("loc", "<red>%d <green>%d <blue>%d</red>".formatted(toTest.blockX(), toTest.blockY(), toTest.blockZ())));
  }

  @Executes("column-fine")
  void columnFine(CommandSender sender, ColumnFinePosition pos) {
    sender.sendRichMessage("You entered: <red><x></red> <blue><z></blue>!",
        Placeholder.parsed("x", Double.toString(pos.x())),
        Placeholder.parsed("x", Double.toString(pos.x()))
    );
  }

  @Executes("column-block")
  void columnBlock(CommandSender sender, ColumnBlockPosition pos) {
    sender.sendRichMessage("You entered: <red><x></red> <blue><z></blue>!",
        Placeholder.parsed("x", Integer.toString(pos.blockX())),
        Placeholder.parsed("x", Integer.toString(pos.blockZ()))
    );
  }

  @Executes("hex-color")
  void hexColor(CommandSender sender, TextColor color) {
    sender.sendRichMessage("Your color: <t_color>Whatever this is, lol</t_color>",
        Placeholder.styling("t_color", color)
    );
  }
}
