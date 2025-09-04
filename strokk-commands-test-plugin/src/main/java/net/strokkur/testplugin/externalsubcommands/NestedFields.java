package net.strokkur.testplugin.externalsubcommands;

import io.papermc.paper.command.brigadier.Commands;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Subcommand;
import org.bukkit.command.CommandSender;

@Command("nestedfields")
class NestedFields {

    @Subcommand("first")
    FirstNesting firstNesting;

    static {
        // Expectation
        final NestedFields instance = new NestedFields();
        instance.firstNesting = new FirstNesting();
        instance.firstNesting.secondNesting = new SecondNesting();

        var built = Commands.literal("nestedfields")
            .then(Commands.literal("first")
                .then(Commands.literal("second")
                    .executes(ctx -> {
                        instance.firstNesting.secondNesting.execute(
                            ctx.getSource().getSender()
                        );
                        return 1;
                    })
                )
            )
            .build();
    }

    static class FirstNesting {

        @Subcommand("second")
        SecondNesting secondNesting;
    }

    static class SecondNesting {

        @Executes
        void execute(CommandSender sender) {
            sender.sendMessage("Wohoo");
        }
    }
}
