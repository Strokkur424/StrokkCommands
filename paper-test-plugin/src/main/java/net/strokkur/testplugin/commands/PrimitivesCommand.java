package net.strokkur.testplugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.arguments.StringArg;
import org.bukkit.command.CommandSender;

import static net.strokkur.commands.objects.arguments.StringArgType.GREEDY;
import static net.strokkur.commands.objects.arguments.StringArgType.STRING;

@Command("primitive")
class PrimitivesCommand {

    @Executes("bool")
    void valueType(CommandSender sender, boolean value) {
        sender.sendRichMessage("<green>You entered: <white><value>",
            Placeholder.component("value", Component.text(value))
        );
    }

    @Executes("int")
    void valueType(CommandSender sender, int value) {
        sender.sendRichMessage("<green>You entered: <white><value>",
            Placeholder.component("value", Component.text(value))
        );
    }

    @Executes("long")
    void valueType(CommandSender sender, long value) {
        sender.sendRichMessage("<green>You entered: <white><value>",
            Placeholder.component("value", Component.text(value))
        );
    }

    @Executes("float")
    void valueType(CommandSender sender, float value) {
        sender.sendRichMessage("<green>You entered: <white><value>",
            Placeholder.component("value", Component.text(value))
        );
    }

    @Executes("double")
    void valueType(CommandSender sender, double value) {
        sender.sendRichMessage("<green>You entered: <white><value>",
            Placeholder.component("value", Component.text(value))
        );
    }

    @Executes("word")
    void wordType(CommandSender sender, String value) {
        sender.sendRichMessage("<green>You entered: <white><value>",
            Placeholder.component("value", Component.text(value))
        );
    }

    @Executes("string")
    void stringType(CommandSender sender, @StringArg(STRING) String value) {
        sender.sendRichMessage("<green>You entered: <white><value>",
            Placeholder.component("value", Component.text(value))
        );
    }

    @Executes("greedy")
    void greedyType(CommandSender sender, @StringArg(GREEDY) String value) {
        sender.sendRichMessage("<green>You entered: <white><value>",
            Placeholder.component("value", Component.text(value))
        );
    }
}