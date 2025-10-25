/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.testplugin.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
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

  @Executes("one-player-profile")
  void execute(CommandSender sender, PlayerProfile profile) {
    sender.sendRichMessage("<green>Found player: <gradient:aqua:blue><profile>",
        Placeholder.unparsed("profile", profile.getName())
    );
  }
}
