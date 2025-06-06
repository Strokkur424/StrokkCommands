package net.strokkur.testplugin.commands;

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("simple")
@Aliases("testsimple")
@Description("A very simple test command to show how annotations work")
public class SimpleCommand {
    
    @Executes
    void noArgs(CommandSender sender, @Executor Player executor) {
        sender.sendRichMessage("<green>Success! The executing player is " + executor.getName());
        executor.sendRichMessage("<gradient:gold:yellow>Hehe very good!");
    }
}