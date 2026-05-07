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
package net.strokkur.testplugin.velocity.wrapper;

import com.mojang.brigadier.Command;
import com.velocitypowered.api.command.CommandSource;
import net.strokkur.commands.CustomExecutorWrapper;
import net.strokkur.commands.Executes;
import org.slf4j.Logger;

import java.time.Duration;

@VelocityWrapperTest.Timing
@net.strokkur.commands.Command("wrapper")
class VelocityWrapperTest {
  private final Logger logger;

  public VelocityWrapperTest(final Logger logger) {
    this.logger = logger;
  }

  @Timing
  Command<CommandSource> wrap(Command<CommandSource> command) {
    return ctx -> {
      final long ns = System.nanoTime();
      try {
        return command.run(ctx);
      } finally {
        logger.info("The command took about {} nanoseconds!", System.nanoTime() - ns);
      }
    };
  }

  @Executes
  void run() {
    try {
      Thread.sleep(Duration.ofMillis(15));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @CustomExecutorWrapper
  @interface Timing {}
}
