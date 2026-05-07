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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.strokkur.commands.CustomExecutorWrapper;
import net.strokkur.commands.Executes;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

@CustomExecutorWrapper
@interface TimingWrapper {}

@Retention(RetentionPolicy.RUNTIME)
@interface Logged {
  String value() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@interface RequiresConfirmation {}

@net.strokkur.commands.Command("wrappertest")
@TimingWrapper
class ExecutorWrapperCommand {

  @TimingWrapper
  public static Command<CommandSourceStack> timingWrapper(Command<CommandSourceStack> executor, Method method) {
    return ctx -> {
      final CommandSender sender = ctx.getSource().getSender();

      // Check for @Logged annotation
      if (method.isAnnotationPresent(Logged.class)) {
        final Logged logged = method.getAnnotation(Logged.class);
        final String logMessage = logged.value().isEmpty() ? method.getName() : logged.value();
        sender.sendRichMessage("<gray>[LOG] Executing: <white>" + logMessage);
      }

      // Check for @RequiresConfirmation annotation
      if (method.isAnnotationPresent(RequiresConfirmation.class)) {
        sender.sendRichMessage("<yellow>[WARN] This action requires confirmation!");
      }

      // Measure execution time
      final long start = System.nanoTime();
      try {
        return executor.run(ctx);
      } catch (CommandSyntaxException e) {
        throw e;
      } catch (Exception e) {
        sender.sendRichMessage("<red>Error: " + e.getMessage());
        return 0;
      } finally {
        final long duration = (System.nanoTime() - start) / 1_000_000;
        sender.sendRichMessage("<gray>[PERF] Execution took <white>" + duration + "ms");
      }
    };
  }

  @Executes("simple")
  void simpleCommand(CommandSender sender) {
    sender.sendRichMessage("<green>Simple command executed!");
  }

  @Logged("Important action being performed")
  @Executes("logged")
  void loggedCommand(CommandSender sender) {
    sender.sendRichMessage("<green>Logged command executed!");
  }

  @RequiresConfirmation
  @Executes("confirm")
  void confirmCommand(CommandSender sender) {
    sender.sendRichMessage("<green>Confirmation command executed!");
  }

  @Logged
  @RequiresConfirmation
  @Executes("both")
  void bothAnnotationsCommand(CommandSender sender) {
    sender.sendRichMessage("<green>Command with both annotations executed!");
  }

  @Executes("witharg")
  void withArgument(CommandSender sender, String message) {
    sender.sendRichMessage("<green>Message received: <white>" + message);
  }
}
