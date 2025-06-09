package net.strokkur.testplugin.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;

@Command("entityargs")
class EntitiesCommand {

    @Executes("entity")
    void execute(CommandSender sender, Entity entity) {
        sender.sendRichMessage("<green>You selected: <white><entity>",
            Placeholder.component("entity", entity.name())
        );
    }

    @Executes("players")
    void execute(CommandSender sender, Collection<Player> players) {
        players.forEach(p -> p.sendRichMessage("<rainbow><b>BOOP!</rainbow> <light_purple>You have been booped by <sender>",
            Placeholder.component("sender", sender.name())
        ));
    }

    @Executes("playerprofiles")
    void execute(CommandSender sender, PlayerProfile[] profiles) {
        sender.sendRichMessage("<green>Found players: <gradient:aqua:blue><profiles>",
            Placeholder.unparsed("profiles", String.join(", ", Arrays.stream(profiles).map(PlayerProfile::getName).toList()))
        );
    }
}
