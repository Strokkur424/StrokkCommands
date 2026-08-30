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

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.OptionalInt;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Command("optional-args")
class OptionalArguments {

  @Executes
  void giveItem(
    CommandContext<CommandSourceStack> ctx,
    ItemStack item,
    Optional<Player> target,
    OptionalInt amount
  ) {
    final Player targetPlayer = target.orElseGet(() -> (Player) ctx.getSource().getExecutor());
    final int targetAmount = amount.orElse(1);
    item.setAmount(targetAmount);
    targetPlayer.give(item);
    ctx.getSource().getSender().sendRichMessage("<green>Successfully gave <white><target> <count> <item></white>!",
      Placeholder.component("target", targetPlayer.displayName()),
      Placeholder.component("count", Component.text(targetAmount)),
      Placeholder.component("item", item.effectiveName())
    );
  }
}
