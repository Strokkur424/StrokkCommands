package net.strokkur.testplugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.annotations.arguments.TimeArg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command("paperargs")
class PaperArgumentsCommand {

    @Executes("time")
    void executes(CommandSender sender, @Executor Entity entity, @TimeArg int time) {
        entity.getWorld().setTime(entity.getWorld().getTime() + time);
        sender.sendRichMessage("<aqua>Successfully fast-forwarded the world's time by <red><amount> ticks</red>!",
            Placeholder.unparsed("amount", Integer.toString(time))
        );
    }

    @Executes("item")
    void executes(CommandSender sender, @Executor Player player, ItemStack itemStack) {
        player.give(itemStack);
        sender.sendRichMessage("<aqua>Successfully gave <player> a <red><item></red>!",
            Placeholder.component("player", player.displayName()),
            Placeholder.component("item", Component.translatable(itemStack))
        );
    }
}
