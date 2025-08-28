package net.strokkur.testplugin.flattening;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Literal;
import org.bukkit.command.CommandSender;

@Command("single-literals")
class SingleLiterals {

    @Executes
    void accept(CommandSender sender, @Literal String accept) {
        sender.sendRichMessage("<green>Accepted!");
    }

    @Executes
    void decline(CommandSender sender, @Literal String decline) {
        sender.sendRichMessage("<red>Declined!");
    }
}
