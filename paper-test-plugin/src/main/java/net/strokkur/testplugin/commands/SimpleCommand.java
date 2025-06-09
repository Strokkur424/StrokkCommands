package net.strokkur.testplugin.commands;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.Literal;
import net.strokkur.commands.annotations.arguments.IntArg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

//@Command("simple")
@Aliases("testsimple")
@Description("A very simple test command to show how annotations work")
public class SimpleCommand {

    @Executes
    void noArgs(CommandSender sender, @Executor Player executor) {
        sender.sendRichMessage("<green>Success! The executing player is " + executor.getName());
        executor.sendRichMessage("<gradient:gold:yellow>Hehe very good!");
    }

    @Executes
    void firstName(CommandSender sender, String firstName) {
        // ...
    }

    @Executes
    void nameArg(CommandSender sender, String firstName, String name) {
        sender.sendRichMessage("<green>You put in: <name>", Placeholder.unparsed("name", name));
    }

    @Executes
    void floatArg(CommandSender sender, String firstName, float value) {
        sender.sendRichMessage("<transition:red:blue:%s>This is some transitioning text :P (%s)".formatted(value, value));
    }

    @Executes("literal test")
    void literalTest(CommandSender sender, @Literal({"one", "two", "three"}) String literal, @IntArg(min = 1, max = 3) int value) {
        int actual = switch (literal) {
            case "one" -> 1;
            case "two" -> 2;
            case "three" -> 3;
            default -> throw new UnsupportedOperationException("Incorrect literal");
        };

        sender.sendPlainMessage("Your input is " + literal + ", which " + (actual == value ? "matches" : "doesn't match") + " your input value!");
    }
}