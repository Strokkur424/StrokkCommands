package net.strokkur.testplugin.subcommands;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.RequiresOP;
import net.strokkur.testplugin.iceacream.IceCreamArgument;
import net.strokkur.testplugin.iceacream.IceCreamCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("subcommands")
public class BetterSubCommands {

    @Command("help")
    class Help {

        @Executes
        void help(CommandSender sender) {
            sender.sendRichMessage("Not a help message.");
        }
    }

    @Command("give")
    record Give(Player target) {

        @Executes("holy-relic")
        void holyRelic(CommandSender sender) {

        }

        @Executes("excalibur")
        void excalibur(CommandSender sender) {

        }
    }
    
    @Command("kill")
    @RequiresOP
    record Kill(Player target, String reason) {

        @Executes
        void kill(CommandSender sender) {
            killForce(sender, false);
        }

        @Executes
        void killForce(CommandSender sender, boolean force) {
            if (!force) {
                return;
            }
            
            target.setHealth(0d);
            sender.sendRichMessage("<red>Successfully killed <target>",
                Placeholder.component("target", target.displayName())
            );
        }
    }
}
