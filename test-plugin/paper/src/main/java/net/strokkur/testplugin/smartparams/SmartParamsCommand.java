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
package net.strokkur.testplugin.smartparams;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Literal;
import net.strokkur.commands.meta.StrokkCommandsDebug;
import net.strokkur.commands.paper.Executor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

@Command("smartparams")
class SmartParamsCommand {
  private final Logger logger;

  public SmartParamsCommand(final Logger logger) {
    this.logger = logger;
  }

  @Executes("no-params")
  void noParameters() {
    logger.info("noParameters() was executed!");
  }

  @Executes("source")
  void wholeSourceStack(final CommandSourceStack source) {
    source.getSender().sendRichMessage("Hey! You are currently at <red>%s <green>%s <blue>%s</red>!".formatted(
        source.getLocation().getBlockX(), source.getLocation().getBlockY(), source.getLocation().getBlockZ()
    ));
  }

  @Executes("all-of-them")
  void allOfEm(final CommandContext<CommandSourceStack> ctx, final CommandSourceStack source, final CommandSender sender) {
    sender.sendRichMessage("<gold>Your entire command input: <u>" + ctx.getInput());
    sender.sendRichMessage("<gold>This command was executed as <executor>",
        Placeholder.component("executor", source.getExecutor() == null
            ? Component.text("null")
            : source.getExecutor().name())
    );
  }

  /// Idk why you would ever do this, but you can!
  @Executes("between-arg")
  void betweenArg(final String wordArg, final CommandSender sender, final @Executor Player executor, int value, String[] allArgs) {
    sender.sendRichMessage("<executor> ran <gold>/<cmd></gold> with <aqua><word></aqua> and <green><value></green>",
        Placeholder.component("executor", executor.displayName()),
        Placeholder.unparsed("cmd", allArgs[0]),
        Placeholder.unparsed("word", wordArg),
        Placeholder.component("value", Component.text(value))
    );
  }

  @Executes("involving-multi-args")
  void multipleArgs(final CommandSourceStack source, final @Literal({"first", "second", "third"}) String choice) {
    source.getSender().sendRichMessage("You chose: " + choice);
  }
}
