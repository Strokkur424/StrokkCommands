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
package net.strokkur.commands.internal.fabric.util;

import net.strokkur.commands.internal.modded.util.ModdedClasses;

public interface FabricClasses extends ModdedClasses {
  // Fabric server classes
  String MOD_INITIALIZER = "net.fabricmc.api.ModInitializer";
  String COMMAND_REGISTRATION_CALLBACK = "net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback";

  // Fabric client classes
  String CLIENT_MOD_INITIALIZER = "net.fabricmc.api.ClientModInitializer";
  String FABRIC_CLIENT_COMMAND_SOURCE = "net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource";
  String CLIENT_COMMAND_MANAGER = "net.fabricmc.fabric.api.client.command.v2.ClientCommandManager";
  String CLIENT_COMMAND_REGISTRATION_CALLBACK = "net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback";
}
