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
package net.strokkur.testplugin.docs.gamemode.preset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Executes;
import net.strokkur.commands.paper.Executor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("ClassCanBeRecord")
@NullMarked
public class GameModePreset {

  private final GameMode mode;

  public GameModePreset(final GameMode mode) {
    this.mode = mode;
  }

  @Executes
  public void executes(CommandSender sender, @Executor Player executor) {
    changeGameMode(sender, executor);
  }

  @Executes
  public void executesTarget(CommandSender sender, Player target) {
    changeGameMode(sender, target);
  }

  private void changeGameMode(CommandSender sender, Player target) {
    final String targetName = sender == target ? "your" : target.getName() + "'s";
    final String targetNameUpper = sender == target ? "Your" : targetName;

    if (target.getGameMode() == mode) {
      sender.sendRichMessage("<red><target_name> game mode is already set to <mode>!",
          Placeholder.unparsed("target_name", targetNameUpper),
          Placeholder.component("mode", Component.translatable(mode))
      );
      return;
    }

    target.setGameMode(mode);
    sender.sendRichMessage("<green>Successfully set <target_name> game mode to <mode>!",
        Placeholder.unparsed("target_name", targetName),
        Placeholder.component("mode", Component.translatable(mode))
    );
  }
}
