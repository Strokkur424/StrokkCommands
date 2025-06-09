package net.strokkur.testplugin.commands;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.arguments.FinePosArg;
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
