package net.strokkur.testplugin.subcommands;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Literal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("lol")
record LolCommand(Player player, @Literal String now) {

    @Executes
    void execute(CommandSender sender) {
        player.sendPlainMessage("lol from " + sender.getName());
        sender.sendPlainMessage("lol'd " + player.getName());
    }
}
