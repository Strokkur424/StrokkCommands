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
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.testplugin.docs.gamemode.preset.GameModePreset;
import org.bukkit.GameMode;

@Command("gamemode")
@Aliases("gm")
class GameModeCommand {

    @Subcommand("survival")
    @Permission("testplugin.gamemode.survival")
    GameModePreset survival = new GameModePreset(GameMode.SURVIVAL);

    @Subcommand("creative")
    @Permission("testplugin.gamemode.creative")
    GameModePreset creative = new GameModePreset(GameMode.CREATIVE);

    @Subcommand("adventure")
    @Permission("testplugin.gamemode.adventure")
    GameModePreset adventure = new GameModePreset(GameMode.ADVENTURE);

    @Subcommand("spectator")
    @Permission("testplugin.gamemode.spectator")
    GameModePreset spectator = new GameModePreset(GameMode.SPECTATOR);
}
