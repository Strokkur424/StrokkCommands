package net.strokkur.testplugin.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.arguments.Arg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("simple")
@Aliases("testsimple")
@Description("A very simple test command to show how annotations work")
public class SimpleCommand {

    @Executes
    private void noArgs(CommandSender sender, @Executor Player executor) {
        sender.sendRichMessage("<green>Success! The executing player is " + executor.getName());
        executor.sendRichMessage("<gradient:gold:yellow>Hehe very good!");
    }

    @Executes
    private void nameArg(CommandSender sender,
                         @Arg("name") String name) {
        sender.sendRichMessage("<green>You put in: <name>", Placeholder.unparsed("name", name));
    }

    @Executes
    private void floatArg(CommandSender sender,
                          @Arg("value") float value) {
        sender.sendRichMessage("<transition:red:blue:%s>This is some transitioning text :P (%s)".formatted(value, value));
    }
}