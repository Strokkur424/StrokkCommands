package net.strokkur.testplugin.externalsubcommands;

import io.papermc.paper.command.brigadier.Commands;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Subcommand;
import org.bukkit.command.CommandSender;

@Command("supernested")
class SuperNested {

    static {
        // Expectation
        final SuperNested instance = new SuperNested();
        final SuperNested.InnerNonStatic instanceInnerNonStatic = instance.new InnerNonStatic();
        final SuperNested.NestedClass instanceInnerNonStaticNested = new SuperNested.NestedClass();
        final SuperNested.NestedClass.UltraNested instanceInnerNonStaticNestedUltraNested = instanceInnerNonStaticNested.new UltraNested();

        var built = Commands.literal("supernested")
            .then(Commands.literal("nonstatic")
                .then(Commands.literal("ULTRA-NESTED")
                    .executes(ctx -> {
                        instanceInnerNonStaticNestedUltraNested.execute(
                            ctx.getSource().getSender()
                        );
                        return 1;
                    })
                )
            )
            .build();
    }

    @Subcommand("nonstatic")
    class InnerNonStatic {

        @Subcommand
        NestedClass nested;
    }

    static class NestedClass {

        @Subcommand("ULTRA-NESTED")
        class UltraNested {

            @Executes
            void execute(CommandSender sender) {
                sender.sendRichMessage("<rainbow>SUPER ULTRA NESTED CLASS WOWOOOOWOWWOWOW");
            }
        }
    }
}
