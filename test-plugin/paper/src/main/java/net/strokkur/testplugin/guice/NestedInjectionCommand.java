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
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.UseInjection;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

@Command("nested-injection-test")
@UseInjection
class NestedInjectionCommand {
  private @Inject JavaPlugin plugin;

  @Subcommand("first") final SomeCommonClass firstField = new SomeCommonClass("<gradient:red:blue>first");

  @Subcommand("second") final SomeCommonClass secondField = new SomeCommonClass("<gradient:green:yellow>second");

  @Executes
  void run() {
    plugin.getLogger().warning("The plugin name is: " + plugin.getName());
  }

  @Subcommand("inner-static")
  static class InnerStatic {
    private @Inject Logger logger;
    private @MyMagicNumber
    @Inject int num;

    @Executes
    void run() {
      logger.info("Magic number: {}", num);
    }
  }

  @Subcommand("inner-nonstatic")
  static class InnerNonStatic {
    private @Inject NestedInjectionCommand parent;

    @Executes
    void run() {
      parent.plugin.getLogger().info("Hi from " + this.getClass().getTypeName());
    }
  }

  static class SomeCommonClass {
    private final String str;

    SomeCommonClass(String str) {
      this.str = str;
    }

    @Executes
    void run(CommandSender sender) {
      sender.sendRichMessage(str);
    }
  }
}
