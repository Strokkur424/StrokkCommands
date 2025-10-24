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
package net.strokkur.testplugin.docs.gamemode;

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Permission;
import net.strokkur.testplugin.docs.gamemode.preset.GameModePreset;
import org.bukkit.GameMode;

@Aliases("gm")
class GameModeCommand {

  @Permission("testplugin.gamemode.survival")
  GameModePreset survival = new GameModePreset(GameMode.SURVIVAL);

  @Permission("testplugin.gamemode.survival")
  GameModePreset s = new GameModePreset(GameMode.SURVIVAL);

  @Permission("testplugin.gamemode.creative")
  GameModePreset creative = new GameModePreset(GameMode.CREATIVE);

  @Permission("testplugin.gamemode.creative")
  GameModePreset c = new GameModePreset(GameMode.CREATIVE);

  @Permission("testplugin.gamemode.adventure")
  GameModePreset adventure = new GameModePreset(GameMode.ADVENTURE);

  @Permission("testplugin.gamemode.adventure")
  GameModePreset a = new GameModePreset(GameMode.ADVENTURE);

  @Permission("testplugin.gamemode.spectator")
  GameModePreset spectator = new GameModePreset(GameMode.SPECTATOR);

  @Permission("testplugin.gamemode.spectator")
  GameModePreset sp = new GameModePreset(GameMode.SPECTATOR);
}
