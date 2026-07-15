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

import net.strokkur.jap.code.convert.ConvertToClassType;
import net.strokkur.jap.code.type.CodeTypes;

public interface VelocityClasses extends ConvertToClassType {
  VelocityClasses COMMAND_SOURCE = create("com.velocitypowered.api.command.CommandSource");
  VelocityClasses PLAYER = create("com.velocitypowered.api.proxy.Player");
  VelocityClasses CONSOLE_COMMAND_SOURCE = create("com.velocitypowered.api.proxy.ConsoleCommandSource");

  VelocityClasses BRIGADIER_COMMAND = create("com.velocitypowered.api.command.BrigadierCommand");
  VelocityClasses COMMAND_META = create("com.velocitypowered.api.command.CommandMeta");

  VelocityClasses PROXY_INITIALIZE_EVENT = create("com.velocitypowered.api.event.proxy.ProxyInitializeEvent");
  VelocityClasses PROXY_SERVER = create("com.velocitypowered.api.proxy.ProxyServer");

  static VelocityClasses create(String fqn) {
    return () -> CodeTypes.ofClass(fqn);
  }
}
