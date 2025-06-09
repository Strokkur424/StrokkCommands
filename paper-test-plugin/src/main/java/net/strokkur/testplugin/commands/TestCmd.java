package net.strokkur.testplugin.commands;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import org.bukkit.command.CommandSender;

@Command("test")
public class TestCmd {

    @Executes("literal")
    void executeOne(CommandSender sender) {
        sender.sendPlainMessage("One!");
    }

    @Executes("literal two")
    void executeTwo(CommandSender sender) {
        sender.sendPlainMessage("Two!");
    }
}