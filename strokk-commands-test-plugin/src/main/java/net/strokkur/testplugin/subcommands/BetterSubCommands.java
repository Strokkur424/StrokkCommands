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
package net.strokkur.testplugin.subcommands;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.RequiresOP;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("subcommands")
public class BetterSubCommands {

    @Command("give")
    record Give(Player target) {

        @Executes("holy-relic")
        void holyRelic(CommandSender sender) {

        }

        @Executes("excalibur")
        void excalibur(CommandSender sender) {

        }
    }

    @Command("kill")
    @RequiresOP
    record Kill(Player target, String reason) {

        @Executes
        void kill(CommandSender sender) {
            killForce(sender, false);
        }

        @Executes
        void killForce(CommandSender sender, boolean force) {
            if (!force) {
                return;
            }

            target.setHealth(0d);
            sender.sendRichMessage("<red>Successfully killed <target>",
                Placeholder.component("target", target.displayName())
            );
        }
    }

    @Command("help")
    class Help {

        @Executes
        void help(CommandSender sender) {
            sender.sendRichMessage("Not a help message.");
        }
    }
}
