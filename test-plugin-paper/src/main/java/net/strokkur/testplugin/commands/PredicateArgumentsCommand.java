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

import com.google.common.collect.Range;
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Literal;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

@Command("does")
class PredicateArgumentsCommand {

  @Executes("item")
  void executor(CommandSender sender, ItemStack item, @Literal("match") String $match, ItemStackPredicate predicate) {
    if (predicate.test(item)) {
      sender.sendMessage(Component.text("Yes, yes it does.", NamedTextColor.GREEN));
    } else {
      sender.sendMessage(Component.text("No, of course it doesn't ;-;", NamedTextColor.RED));
    }
  }

  @Executes("number")
  void executor(CommandSender sender, double value, @Literal("fit") String $fit, @Literal("into") String $into, Range<Double> range) {
    checkNumberInRange(sender, value, range);
  }

  @Executes("int-range")
  void executor(CommandSender sender, int value, @Literal("fit") String $fit, @Literal("into") String $into, Range<Integer> range) {
    checkNumberInRange(sender, value, range);
  }

  <T extends Comparable> void checkNumberInRange(Audience audience, T value, Range<T> range) {
    if (range.contains(value)) {
      audience.sendMessage(Component.text("Yes it does, open your eyes.", NamedTextColor.GREEN));
    } else {
      audience.sendMessage(Component.text("W... why the hell would it???", NamedTextColor.RED));
    }
  }
}
