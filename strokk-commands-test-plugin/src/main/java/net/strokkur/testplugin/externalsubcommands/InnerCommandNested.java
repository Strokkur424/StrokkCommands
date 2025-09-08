package net.strokkur.testplugin.externalsubcommands;

import io.papermc.paper.command.brigadier.Commands;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Subcommand;
import org.bukkit.command.CommandSender;

@Command("innercommandnested")
class InnerCommandNested {

    static {
        // Expectation
        final InnerCommandNested instance = new InnerCommandNested();
        final InnerCommandNested.Nested instanceNested = instance.new Nested();
        final InnerCommandNested.MyNestedClass instanceNestedMyNestedClass = instanceNested.myNestedClass;

        var built = Commands.literal("innercommandnested")
            .then(Commands.literal("nested")
                .executes(ctx -> {
                    instanceNestedMyNestedClass.execute(
                        ctx.getSource().getSender()
                    );
                    return 1;
                })
            )
            .build();
    }

    @Subcommand("nested")
    class Nested {

        @Subcommand
        MyNestedClass myNestedClass;
    }

    static class MyNestedClass {

        @Executes("a")
        void execute(CommandSender sender) {
            sender.sendRichMessage("<green>Hi double nested.");
        }
    }
}
