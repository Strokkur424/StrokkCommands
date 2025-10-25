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

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.paper.arguments.FinePosArg;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnstableApiUsage")
@Command("locationargs")
class LocationArgumentsCommand {

  @Executes("blockpos")
  void executes(CommandSender sender, BlockPosition blockPos) {
    sender.sendRichMessage("<green>Your input is at: <white><pos>",
        Placeholder.unparsed("pos", "x: %s y: %s z: %s".formatted(blockPos.x(), blockPos.y(), blockPos.z()))
    );
  }

  @Executes("finepos")
  void executes(CommandSender sender, FinePosition finePos) {
    sender.sendRichMessage("<green>Your input is at: <white><pos>",
        Placeholder.unparsed("pos", "x: %s y: %s z: %s".formatted(finePos.x(), finePos.y(), finePos.z()))
    );
  }

  @Executes("finepos center")
  void executesCenter(CommandSender sender, @FinePosArg(true) FinePosition finePos) {
    sender.sendRichMessage("<green>Your input is at: <white><pos>",
        Placeholder.unparsed("pos", "x: %s y: %s z: %s".formatted(finePos.x(), finePos.y(), finePos.z()))
    );
  }

  @Executes("world")
  void executes(CommandSender sender, World world) {
    sender.sendRichMessage("<green>You entered: <white><world>",
        Placeholder.unparsed("world", world.getName())
    );
  }
}
