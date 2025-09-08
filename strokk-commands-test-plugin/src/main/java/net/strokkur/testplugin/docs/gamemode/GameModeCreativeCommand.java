package net.strokkur.testplugin.docs.gamemode;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.testplugin.docs.gamemode.preset.GameModePreset;
import org.bukkit.GameMode;

@Command("gmc")
class GameModeCreativeCommand {

    @Subcommand
    GameModePreset gameMode = new GameModePreset(GameMode.CREATIVE);
}
