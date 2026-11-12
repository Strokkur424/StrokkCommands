package net.strokkur.testplugin.commands;

import net.strokkur.commands.Aliases;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.paper.Description;
import net.strokkur.commands.paper.Executor;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("enderchest")
@Aliases("ec")
@Description("View a player's enderchest")
@Permission("inventories.command.enderchest")
public class RootExecArgsRequirementsCommand {

  @Executes
  void onExecute(CommandSender sender, @Executor Player senderPlayer) {
    // ...
  }

  @Executes
  @Permission("inventories.command.enderchest.other")
  void onExecuteOther(CommandSender sender, @Executor Player senderPlayer, Player target) {
    // ...
  }
}
