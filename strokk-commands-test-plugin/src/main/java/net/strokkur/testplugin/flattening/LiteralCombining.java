package net.strokkur.testplugin.flattening;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Literal;
import org.bukkit.command.CommandSender;

@Command("combined")
class LiteralCombining {

    @Executes("sub")
    void normalSub(CommandSender sender) {
        sender.sendRichMessage("<gold>Normal executes method.");
    }

    @Executes
    void literalSub(CommandSender sender, @Literal String sub, @Literal String two) {
        sender.sendRichMessage("<gold>Literal executes method.");
    }

    @Command("sub class")
    static class SubClass {

        @Executes
        void execute(CommandSender sender) {
            sender.sendRichMessage("<gold>Sub class executes method.");
        }
    }
}
