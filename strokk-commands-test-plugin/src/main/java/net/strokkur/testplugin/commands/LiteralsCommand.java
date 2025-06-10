package net.strokkur.testplugin.commands;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Literal;
import org.bukkit.command.CommandSender;

@Command("literals")
public class LiteralsCommand {

    @Executes("hey there, how")
    void executes(CommandSender sender,
                  @Literal({"are", "am"}) String first,
                  @Literal({"you", "I"}) String second,
                  @Literal("doing?") String $doing) {
        
        if (first.equals("are") && second.equals("you")) {
            sender.sendMessage("I am doing great, thanks for asking :)");
        } else if (first.equals("am") && second.equals("I")) {
            sender.sendMessage("I hope you are doing good?");
        } else {
            sender.sendRichMessage("<red>Dude, what does that even mean");
        }
    }
}
