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
package net.strokkur.testplugin.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.strokkur.commands.Command;
import net.strokkur.commands.CustomRequirement;
import net.strokkur.commands.Executes;

@Command("requirements-test")
@MyRequirement
class RequirementsTest {

  @Executes
  void execute(Player player) {
    player.sendPlainMessage("This command is clearly rigged.");
  }

  @MyRequirement
  static boolean require(final CommandSource source) {
    return source instanceof Player player && player.getUsername().equals("Strokkur24");
  }
}

@CustomRequirement
@interface MyRequirement {}

