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

import com.velocitypowered.api.proxy.ProxyServer;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.testplugin.velocity.TestPluginVelocity;

@Command("propagation")
class ConstructorPropagation {
  private final TestPluginVelocity plugin;
  private final ProxyServer server;

  public ConstructorPropagation(final TestPluginVelocity plugin, final ProxyServer server) {
    this.plugin = plugin;
    this.server = server;
  }

  @Executes
  void execute() {
    this.plugin.logger().info("There are currently {} players online!", this.server.getAllPlayers().size());
  }
}
