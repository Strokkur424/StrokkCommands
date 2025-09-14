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
package net.strokkur.testplugin.iceacream;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.arguments.CustomArg;
import org.bukkit.command.CommandSender;

@Command("icecream")
public class IceCreamCommand {

  @Executes("lick")
  void onLick(CommandSender sender, @CustomArg(IceCreamArgument.class) IceCreamType iceCream) {
    sender.sendRichMessage("<rainbow><b>YUMMY!</rainbow> You just had a scoop of <color:#7925ab><icecream></color>!",
        Placeholder.unparsed("icecream", iceCream.toString())
    );
  }
}
