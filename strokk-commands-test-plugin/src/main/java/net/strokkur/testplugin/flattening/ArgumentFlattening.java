package net.strokkur.testplugin.flattening;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Literal;
import org.bukkit.command.CommandSender;

@Command("flat-args")
class ArgumentFlattening {

    @Executes
    void stringThenInt(CommandSender sender, String string, int i) {

    }

    @Executes
    void stringThenFloat(CommandSender sender, String string, float i) {

    }

    @Executes
    void stringThenLiteral(CommandSender sender, String string, @Literal String i) {

    }
}
