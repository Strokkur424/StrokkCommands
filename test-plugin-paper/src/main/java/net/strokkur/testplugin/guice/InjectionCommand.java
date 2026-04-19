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
package net.strokkur.testplugin.guice;

import com.google.inject.Inject;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.UseInjection;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

@Command("injection-test")
@UseInjection
class InjectionCommand {
  private @Inject JavaPlugin plugin;
  private final int num;

  @Inject
  InjectionCommand(@MyMagicNumber int num, Logger logger) {
    logger.info("The magic number is: {}", num);
    this.num = num;
  }

  @Executes
  void execute(CommandSender sender) {
    sender.sendRichMessage("<red><plugin></red>'s magic number is <gold><num></gold>",
        Placeholder.unparsed("plugin", plugin.getName()),
        Placeholder.unparsed("num", Integer.toString(num))
    );
  }
}
