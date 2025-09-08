package net.strokkur.testplugin.docs.gamemode.preset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Executor;
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
        if (executor.getGameMode() == mode) {
            sender.sendRichMessage("<red>Your game mode is already set to <mode>!",
                Placeholder.component("mode", Component.translatable(mode))
            );
            return;
        }

        executor.setGameMode(mode);
        sender.sendRichMessage("<green>Successfully set your game mode to <mode>!",
            Placeholder.component("mode", Component.translatable(mode))
        );
    }

    @Executes
    public void executesTarget(CommandSender sender, Player target) {
        if (target.getGameMode() == mode) {
            sender.sendRichMessage("<red><target>'s game mode is already set to <mode>!",
                Placeholder.unparsed("target", target.getName()),
                Placeholder.component("mode", Component.translatable(mode))
            );
            return;
        }

        target.setGameMode(mode);
        sender.sendRichMessage("<green>Successfully set <target>'s game mode to <mode>!",
            Placeholder.unparsed("target", target.getName()),
            Placeholder.component("mode", Component.translatable(mode))
        );
    }
}
