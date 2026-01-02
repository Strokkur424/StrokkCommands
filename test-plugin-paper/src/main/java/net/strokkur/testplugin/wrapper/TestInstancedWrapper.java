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
package net.strokkur.testplugin.wrapper;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.strokkur.commands.CustomExecutorWrapper;
import net.strokkur.commands.Executes;
import net.strokkur.commands.UnsetExecutorWrapper;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@CustomExecutorWrapper
@interface LogWrapper {}

@LogWrapper
@net.strokkur.commands.Command("log-test")
class TestInstancedWrapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestInstancedWrapper.class);

  @LogWrapper
  Command<CommandSourceStack> log(Command<CommandSourceStack> command, Method method) {
    LOGGER.info("Called handler: {}", method.getName());
    return command;
  }

  @Executes
  void execute(CommandSender sender) {
    sender.sendRichMessage("<gold>Hey!");
  }

  @Executes("literal")
  void executeLiteral(CommandSender sender) {
    sender.sendRichMessage("<red>This has a literal!");
  }

  @Executes("unset")
  @UnsetExecutorWrapper
  void executeUnset(CommandSender sender) {
    sender.sendRichMessage("<greed>Wohoo!");
  }
}
