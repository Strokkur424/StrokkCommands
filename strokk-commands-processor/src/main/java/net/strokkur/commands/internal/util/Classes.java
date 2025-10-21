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
package net.strokkur.commands.internal.util;

public interface Classes {
  // Paper types
  String COMMAND_SENDER = "org.bukkit.command.CommandSender";
  String PLAYER = "org.bukkit.entity.Player";
  String ENTITY = "org.bukkit.entity.Entity";
  String COMPONENT = "net.kyori.adventure.text.Component";

  // Java types
  String LIST = "java.util.List";
  String COLLECTIONS = "java.util.Collections";
  String ARRAYS = "java.util.Arrays";
  String LIST_STRING = LIST + "<java.lang.String>";

  // Brigadier types
  String COMMAND = "com.mojang.brigadier.Command";
  String LITERAL_COMMAND_NODE = "com.mojang.brigadier.tree.LiteralCommandNode";
  String COMMAND_SOURCE_STACK = "io.papermc.paper.command.brigadier.CommandSourceStack";
  String COMMANDS = "io.papermc.paper.command.brigadier.Commands";
  String SIMPLE_COMMAND_EXCEPTION_TYPE = "com.mojang.brigadier.exceptions.SimpleCommandExceptionType";
  String MESSAGE_COMPONENT_SERIALIZER = "io.papermc.paper.command.brigadier.MessageComponentSerializer";

  // Other
  String NULL_MARKED = "org.jspecify.annotations.NullMarked";
}
