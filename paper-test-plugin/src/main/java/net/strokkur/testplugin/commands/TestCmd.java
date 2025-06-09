package net.strokkur.testplugin.commands;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.RequiresOP;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("test")
@Permission("some.value")
@RequiresOP
public class TestCmd {

    @Executes
    void executeOne(CommandSender sender, @Executor Player player, String stringArg) {
        sender.sendPlainMessage("One!");
    }
}
