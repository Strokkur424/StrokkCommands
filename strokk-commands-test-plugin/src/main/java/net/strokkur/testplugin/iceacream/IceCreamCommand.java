package net.strokkur.testplugin.iceacream;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.arguments.CustomArg;
import org.bukkit.command.CommandSender;

@Command("icecream")
class IceCreamCommand {

    @Executes("lick")
    void onLick(CommandSender sender, @CustomArg(IceCreamArgument.class) IceCreamType iceCream) {
        sender.sendRichMessage("<rainbow><b>YUMMY!</rainbow> You just had a scoop of <color:#7925ab><icecream></color>!",
            Placeholder.unparsed("icecream", iceCream.toString())
        );
    }
}
