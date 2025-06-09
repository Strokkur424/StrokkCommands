package net.strokkur.testplugin.commands;

import com.google.common.collect.Range;
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Literal;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("UnstableApiUsage")
@Command("does")
public class PredicateArgumentsCommand {

    @Executes("item")
    void executor(CommandSender sender, ItemStack item, @Literal("match") String $match, ItemStackPredicate predicate) {
        if (predicate.test(item)) {
            sender.sendMessage(Component.text("Yes, yes it does.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("No, of course it doesn't ;-;", NamedTextColor.RED));
        }
    }

    @Executes("number")
    void executor(CommandSender sender, double value, @Literal("fit") String $fit, @Literal("into") String $into, Range<Double> range) {
        if (range.contains(value)) {
            sender.sendMessage(Component.text("Yes it does, open your eyes.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("W... why the hell would it???", NamedTextColor.RED));
        }
    }
}
