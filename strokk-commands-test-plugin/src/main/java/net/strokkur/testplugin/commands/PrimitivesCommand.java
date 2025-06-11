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
package net.strokkur.testplugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.arguments.StringArg;
import org.bukkit.command.CommandSender;

import static net.strokkur.commands.StringArgType.GREEDY;
import static net.strokkur.commands.StringArgType.STRING;

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