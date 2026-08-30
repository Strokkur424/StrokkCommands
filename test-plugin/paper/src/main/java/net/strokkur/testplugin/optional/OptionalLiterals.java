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

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Literal;
import net.strokkur.commands.arguments.StringArg;
import net.strokkur.testplugin.TestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static net.strokkur.commands.arguments.StringArgType.GREEDY;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Command("optional-lit")
class OptionalLiterals {

  @Executes
  void withParams(
    CommandSender sender,
    @Literal Optional<String> ticks, @Nullable Integer ticksValue,
    @Literal Optional<String> message, @Nullable @StringArg(GREEDY) String messageValue
  ) {
    Bukkit.getScheduler().runTaskLater(
      TestPlugin.getPlugin(TestPlugin.class),
      () -> sender.sendRichMessage(message.map(ignored -> messageValue).orElse("<gradient:aqua:blue>Default message!")),
      ticks.map(ignored -> ticksValue).orElse(0)
    );
  }
}
