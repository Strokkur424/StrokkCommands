package net.strokkur.commands.internal.fabric.client.mojang.util;

import net.strokkur.commands.internal.util.Classes;

public interface FabricClasses extends Classes {
  // Minecraft classes
  String COMMAND_BUILD_CONTEXT = "net.minecraft.commands.CommandBuildContext";

  // Fabric client classes
  String CLIENT_MOD_INITIALIZER = "net.fabricmc.api.ClientModInitializer";
  String FABRIC_CLIENT_COMMAND_SOURCE = "net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource";
  String CLIENT_COMMAND_MANAGER = "net.fabricmc.fabric.api.client.command.v2.ClientCommandManager";
  String CLIENT_COMMAND_REGISTRATION_CALLBACK = "net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback";
}
