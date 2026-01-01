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
package net.strokkur.testplugin.inherited;

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import org.bukkit.command.CommandSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Test command demonstrating inherited annotations feature.
/// This allows you to create custom annotations that act like built-in ones.
@Command("inheritedtest")
class InheritedAnnotationsCommand {

  /// A custom annotation that inherits from @Executes
  /// When you annotate a method with @HelpExecutes, it behaves like @Executes("help")
  @Executes("help")
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface HelpExecutes {}

  /// A custom annotation that inherits from @Subcommand
  /// When you annotate a class with @AdminSubcommand, it behaves like @Subcommand("admin")
  @Subcommand("admin")
  @Retention(RetentionPolicy.SOURCE)
  @Target({ElementType.TYPE, ElementType.FIELD})
  public @interface AdminSubcommand {}

  /// A custom executes with a path
  @Executes("info")
  @Retention(RetentionPolicy.SOURCE)
  @Target(ElementType.METHOD)
  public @interface InfoExecutes {}

  /// Regular @Executes command
  @Executes("regular")
  void regularCommand(CommandSender sender) {
    sender.sendRichMessage("<green>Regular @Executes command!");
  }

  /// Using inherited @HelpExecutes annotation
  /// This should create a "help" subcommand
  @HelpExecutes
  void helpCommand(CommandSender sender) {
    sender.sendRichMessage("<aqua>Help command using inherited annotation!");
    sender.sendRichMessage("<gray>Commands:");
    sender.sendRichMessage("<gray>- /inheritedtest regular");
    sender.sendRichMessage("<gray>- /inheritedtest help");
    sender.sendRichMessage("<gray>- /inheritedtest info");
    sender.sendRichMessage("<gray>- /inheritedtest admin ...");
  }

  /// Using inherited @InfoExecutes annotation
  @InfoExecutes
  void infoCommand(CommandSender sender) {
    sender.sendRichMessage("<yellow>Info command using inherited annotation!");
  }

  /// Subcommand using inherited @AdminSubcommand annotation
  @AdminSubcommand
  static class AdminCommands {

    @Executes("status")
    void status(CommandSender sender) {
      sender.sendRichMessage("<gold>Admin status: All systems operational!");
    }

    @Executes("reload")
    void reload(CommandSender sender) {
      sender.sendRichMessage("<gold>Configuration reloaded!");
    }
  }
}
