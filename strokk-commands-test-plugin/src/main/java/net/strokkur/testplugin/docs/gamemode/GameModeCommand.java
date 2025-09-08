package net.strokkur.testplugin.docs.gamemode;

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.testplugin.docs.gamemode.preset.GameModePreset;
import org.bukkit.GameMode;

@Command("gamemode")
@Aliases("gm")
class GameModeCommand {

    @Subcommand("survival")
    GameModePreset survival = new GameModePreset(GameMode.SURVIVAL);

    @Subcommand("creative")
    GameModePreset creative = new GameModePreset(GameMode.CREATIVE);

    @Subcommand("adventure")
    GameModePreset adventure = new GameModePreset(GameMode.ADVENTURE);

    @Subcommand("spectator")
    GameModePreset spectator = new GameModePreset(GameMode.SPECTATOR);
}
