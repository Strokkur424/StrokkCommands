package net.strokkur.testplugin.externalsubcommands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Subcommand;
import org.bukkit.command.CommandSender;

@Command("simplefields")
class SimpleFields {

    @Subcommand
    SubBlueprint hello = new SubBlueprint("Hey <sender>, how are you?");

    @Subcommand
    SubBlueprint weather = new SubBlueprint("The weather is nice today, isn't it?");

    @Subcommand
    SubBlueprint balance = new SubBlueprint("You're balance is: <green>$0</green>. Unfortunate.");

    static {
        // Expectations:
        final SimpleFields instance = new SimpleFields();
        final LiteralCommandNode<CommandSourceStack> built = Commands.literal("tellpresets")
            .then(Commands.literal("hello")
                .executes(ctx -> {
                    instance.hello.execute(ctx.getSource().getSender());
                    return 1;
                })
            )
            .then(Commands.literal("weather")
                .executes(ctx -> {
                    instance.weather.execute(ctx.getSource().getSender());
                    return 1;
                })
            )
            .then(Commands.literal("balance")
                .executes(ctx -> {
                    instance.balance.execute(ctx.getSource().getSender());
                    return 1;
                })
            )
            .build();
    }

    // It cannot be a record, as the param would be interpreted as an argument otherwise
    @SuppressWarnings("ClassCanBeRecord")
    static class SubBlueprint {
        private final String text;

        public SubBlueprint(final String text) {
            this.text = text;
        }

        @Executes
        void execute(CommandSender sender) {
            sender.sendRichMessage(text, Placeholder.unparsed("sender", sender.getName()));
        }
    }
}
