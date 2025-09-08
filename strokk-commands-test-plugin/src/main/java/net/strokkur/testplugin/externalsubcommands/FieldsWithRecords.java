package net.strokkur.testplugin.externalsubcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Subcommand;
import org.bukkit.command.CommandSender;

//@Command("recordfields")
class FieldsWithRecords {

    @Subcommand
    SomeRecord someRecord;

    static {
        // Expectation:
        final FieldsWithRecords instance = new FieldsWithRecords();
        var built = Commands.literal("recordfields")
            .then(Commands.argument("wordArg", StringArgumentType.word())
                .executes(ctx -> {
                    final SomeRecord executor = new SomeRecord(
                        StringArgumentType.getString(ctx, "wordArg")
                    );
                    executor.execute(
                        ctx.getSource().getSender()
                    );
                    return 1;
                })
            )
            .build();
    }

    record SomeRecord(String wordArg) {

        @Executes
        void execute(CommandSender sender) {
            sender.sendMessage(wordArg);
        }
    }
}
