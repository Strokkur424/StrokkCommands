package net.strokkur.testplugin.subcommands;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Subcommand;
import org.bukkit.command.CommandSender;

@Command("cleannestedrecord")
class CleanNestedRecordCommand {

    @Subcommand
    record NestedRecord(String word) {

        @Executes
        void execute(CommandSender sender) {
            sender.sendPlainMessage("Hell yeah!");
        }
    }
}
