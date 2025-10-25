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

