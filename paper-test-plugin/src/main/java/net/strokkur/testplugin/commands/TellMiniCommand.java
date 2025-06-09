package net.strokkur.testplugin.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.arguments.StringArg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import static net.strokkur.commands.objects.arguments.StringArgType.GREEDY;

//@Command("tellmini")
class TellMiniCommand {

    @Executes
    void executes(CommandSender sender, @StringArg(GREEDY) String message) {
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<dark_gray>[<b><dark_red>BROADCAST</b>] <red><sender></red> »</dark_gray> <message>",
            Placeholder.component("sender", sender.name()),
            Placeholder.parsed("message", message)
        ));
    }
}