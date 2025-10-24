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
import org.bukkit.command.CommandSender;

class PrimitivesCommand {

  void valueType(CommandSender sender, boolean value) {
    sender.sendRichMessage("<green>You entered: <white><value>",
        Placeholder.component("value", Component.text(value))
    );
  }

  void valueType(CommandSender sender, int value) {
    sender.sendRichMessage("<green>You entered: <white><value>",
        Placeholder.component("value", Component.text(value))
    );
  }

  void valueType(CommandSender sender, long value) {
    sender.sendRichMessage("<green>You entered: <white><value>",
        Placeholder.component("value", Component.text(value))
    );
  }

  void valueType(CommandSender sender, float value) {
    sender.sendRichMessage("<green>You entered: <white><value>",
        Placeholder.component("value", Component.text(value))
    );
  }

  void valueType(CommandSender sender, double value) {
    sender.sendRichMessage("<green>You entered: <white><value>",
        Placeholder.component("value", Component.text(value))
    );
  }

  void wordType(CommandSender sender, String value) {
    sender.sendRichMessage("<green>You entered: <white><value>",
        Placeholder.component("value", Component.text(value))
    );
  }

  void stringType(CommandSender sender, String value) {
    sender.sendRichMessage("<green>You entered: <white><value>",
        Placeholder.component("value", Component.text(value))
    );
  }

  void greedyType(CommandSender sender, String value) {
    sender.sendRichMessage("<green>You entered: <white><value>",
        Placeholder.component("value", Component.text(value))
    );
  }
}
