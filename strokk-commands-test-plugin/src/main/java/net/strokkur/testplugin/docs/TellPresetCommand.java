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
package net.strokkur.testplugin.docs;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Literal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("tellpreset")
public class TellPresetCommand {

    @Executes
    void executeTellPreset(CommandSender sender,
                           Player player,
                           @Literal({"first", "second", "last"}) String preset) {
        String message = switch (preset) {
            case "first" -> "You selected the first choice!";
            case "second" -> "This is the second one...";
            case "last" -> "...and this is the last one.";
            // This will never happen
            default -> throw new IllegalStateException("Illegal literal.");
        };

        player.sendPlainMessage(message);
    }
}
