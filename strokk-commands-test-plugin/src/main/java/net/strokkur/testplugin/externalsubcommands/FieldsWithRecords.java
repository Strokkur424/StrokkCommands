/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.testplugin.externalsubcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Subcommand;
import org.bukkit.command.CommandSender;

@Command("recordfields")
class FieldsWithRecords {

    @Subcommand
    SomeRecord someRecord;

    static {
        // Expectation:
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
