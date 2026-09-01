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
package net.strokkur.testplugin.enumarg;

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.paper.DefaultToExecutor;
import org.bukkit.Axis;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

@Command("enums")
class SomeEnumsCommand {

  @Executes("material")
  void material(Material material) {
    // ...
  }

  @Executes("multiple")
  void multiple(Axis axis, EventPriority priority) {
    // ...
  }

  @Executes("mixed")
  void mixed(Effect effect, @DefaultToExecutor Player target) {
    target.playEffect(target.getLocation(), effect, null);
  }
}
