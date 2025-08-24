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

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

//@Command("firstcommand")
@Aliases("fc")
@Description("My first StrokkCommands-command!")
class MyFirstCommand {

    @Executes("two three four")
    void onExecute(CommandSender sender) {
        sender.sendRichMessage("<#f29def>Hey there! You just executed your first command ^-^");
    }
    
    @Executes("fling")
    void onFling(CommandSender sender, /* @Executor */ Player player) {
        player.setVelocity(player.getVelocity().add(new Vector(0, 10, 0)));
        player.sendRichMessage("<b><#c4e6ff>WOOSH</b> <#c4fffd>You've been flung!");
    }
}