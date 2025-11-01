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
package net.strokkur.testplugin.velocity.reference;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.velocity.Aliases;
import net.strokkur.commands.velocity.Permission;

import java.util.Optional;

@Command("testcommand")
@Aliases("test")
@Permission("testcommand.use")
final class TestCommand {
  private final ProxyServer proxy;

  public TestCommand(final ProxyServer proxy) {
    this.proxy = proxy;
  }

  @Executes
  void execute(CommandSource source) {
    source.sendRichMessage("<gradient:#beeeee:#eeeebb><b>It works! Wohooo!!");
  }

  @Executes("run")
  void run(Player player) {
    player.sendPlainMessage("Velocity commands wohoo!");
  }

  @Executes("run")
  void runWithTarget(CommandSource source, String target) {
    final Optional<Player> targetPlayer = this.proxy.getPlayer(target);

    if (targetPlayer.isEmpty()) {
      source.sendRichMessage("<red>This player does not exist!");
      return;
    }

    targetPlayer.get().sendRichMessage("<aqua>Somebody ran /test run for you!");
    source.sendRichMessage("<green>Ran for <target>",
        Placeholder.parsed("player", targetPlayer.get().getUsername())
    );
  }
}
