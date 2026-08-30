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
package net.strokkur.commands.internal.paper.util;

import net.strokkur.jap.code.convert.ConvertToClassType;
import net.strokkur.jap.code.type.CodeTypes;

public interface PaperClasses extends ConvertToClassType {
  // Paper types
  PaperClasses COMMAND_SENDER = create("org.bukkit.command.CommandSender");
  PaperClasses PLAYER = create("org.bukkit.entity.Player");
  PaperClasses ENTITY = create("org.bukkit.entity.Entity");
  PaperClasses JAVA_PLUGIN = create("org.bukkit.plugin.java.JavaPlugin");
  PaperClasses COMPONENT = create("net.kyori.adventure.text.Component");

  PaperClasses BOOTSTRAP_CONTEXT = create("io.papermc.paper.plugin.bootstrap.BootstrapContext");
  PaperClasses PLUGIN_BOOTSTRAP = create("io.papermc.paper.plugin.bootstrap.PluginBootstrap");

  // Brigadier related
  PaperClasses COMMAND_SOURCE_STACK = create("io.papermc.paper.command.brigadier.CommandSourceStack");
  PaperClasses COMMANDS = create("io.papermc.paper.command.brigadier.Commands");
  PaperClasses MESSAGE_COMPONENT_SERIALIZER = create("io.papermc.paper.command.brigadier.MessageComponentSerializer");

  static PaperClasses create(String fqn) {
    return () -> CodeTypes.ofClass(fqn);
  }
}
