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
package net.strokkur.testplugin.velocity.argument;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import org.slf4j.Logger;

@Command("registeredserver")
class RegisteredServerTest {

  private final Logger logger;

  RegisteredServerTest(Logger logger) {
    this.logger = logger;
  }

  @Executes("info")
  void onInfo(CommandSource sender, RegisteredServer server) {
    server.ping()
      .whenComplete((p, e) -> {
        if (e != null) {
          sender.sendRichMessage("<red>Unexpected error: <error>", Placeholder.unparsed("error", e.getMessage()));
          if (sender instanceof ConsoleCommandSource) {
            logger.error("Error pinging server '{}'", server.getServerInfo().getName(), e);
          }
          return;
        }
        sender.sendRichMessage(
          """
            <green>Viewing server info for <yellow><server>
            <green>Player count: <yellow><count>
            <green>Description: <yellow><description>
            <green>Version name: <yellow><version>
            """,
          Placeholder.unparsed("server", server.getServerInfo().getName()),
          Placeholder.unparsed("count", p.getPlayers().isPresent() ? String.valueOf(p.getPlayers().get().getOnline()) : "0"),
          Placeholder.component("description", p.getDescriptionComponent() == null ? Component.text("No description") : p.getDescriptionComponent()),
          Placeholder.unparsed("version", p.getVersion().getName())
        );
      });
  }

  @Executes("send")
  void onSend(CommandSource sender, Player target, RegisteredServer server) {
    target.createConnectionRequest(server)
      .connect()
      .whenComplete((r, e) -> {
        if (r.isSuccessful()) {
          sender.sendRichMessage("<green>Successfully sent <white><player></white> to <orange><server></orange>!",
            Placeholder.unparsed("player", target.getUsername()),
            Placeholder.unparsed("server", server.getServerInfo().getName()));
          target.sendRichMessage("<yellow>You've been sent to this server by <red><sender>",
            Placeholder.unparsed("sender", (sender instanceof Player playerSender) ? playerSender.getUsername() : "console"));
        } else {
          sender.sendRichMessage("<red>Unexpected problem occurred: <error>",
            Placeholder.unparsed("error", e.getMessage()));
        }
      });
  }

}
