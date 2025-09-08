package net.strokkur.testplugin.externalsubcommands;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Executes;
import org.bukkit.command.CommandSender;

class ExternalSimpleSubBlueprint {
    private final String text;

    public ExternalSimpleSubBlueprint() {
        this("default");
    }

    public ExternalSimpleSubBlueprint(final String text) {
        this.text = text;
    }

    @Executes
    void execute(CommandSender sender) {
        sender.sendRichMessage(text, Placeholder.unparsed("sender", sender.getName()));
    }
}
