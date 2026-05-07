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
package net.strokkur.commands.internal.velocity.util;

import net.strokkur.commands.internal.util.Classes;

public interface VelocityClasses extends Classes {
  String COMMAND_SOURCE = "com.velocitypowered.api.command.CommandSource";
  String PLAYER = "com.velocitypowered.api.proxy.Player";
  String CONSOLE_COMMAND_SOURCE = "com.velocitypowered.api.proxy.ConsoleCommandSource";

  String BRIGADIER_COMMAND = "com.velocitypowered.api.command.BrigadierCommand";
  String COMMAND_META = "com.velocitypowered.api.command.CommandMeta";

  String PROXY_INITIALIZE_EVENT = "com.velocitypowered.api.event.proxy.ProxyInitializeEvent";
  String PROXY_SERVER = "com.velocitypowered.api.proxy.ProxyServer";
}
