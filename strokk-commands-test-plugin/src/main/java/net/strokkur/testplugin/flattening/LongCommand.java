package net.strokkur.testplugin.flattening;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import org.bukkit.command.CommandSender;

@Command("this is a long command")
class LongCommand {

    @Executes
    void execute(CommandSender sender) {
        sender.sendMessage("This is a long command");
    }
}
