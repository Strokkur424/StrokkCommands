package net.strokkur.testmod.fabric.client.commands;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Literal;

@Command("what a cool command,")
public class SimpleCommand {
  private final CommandBuildContext registryAccess;

  public SimpleCommand(final CommandBuildContext registryAccess) {
    this.registryAccess = registryAccess;
  }

  @Executes
  void execute(FabricClientCommandSource source, @Literal String right) {
    source.sendFeedback(Component.literal("Yeah, for sure!"));
  }
}





