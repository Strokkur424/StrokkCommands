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
package net.strokkur.testplugin.velocity.reference;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jspecify.annotations.NullMarked;

/**
 * A class holding the Brigadier source tree generated from
 * {@link TestCommand} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
 *
 * @author Strokkur24 - StrokkCommands
 * @version 2.0.0-SNAPSHOT
 * @see #create(ProxyServer) creating the LiteralArgumentBuilder
 * @see #register(ProxyServer, Object) registering the command
 */
@NullMarked
public final class TestCommandBrigadierRef {

  /**
   * Shortcut for registering the command node returned from
   * {@link #create(ProxyServer)}. This method uses the provided aliases
   * from the original source file.
   *
   * <h3>Registering the command</h3>
   * <p>
   * Commands should only be registered during the {@link ProxyInitializeEvent}.
   * The example below shows an example of how to do this. For more information,
   * refer to <a href="https://docs.papermc.io/velocity/dev/command-api/#registering-a-command">The Velocity Command API docs</a>
   *
   * <pre>{@code
   * @Subscribe
   * void onProxyInitialize(final ProxyInitializeEvent event) {
   *   TestCommandBrigadier.register(this.proxy, this);
   * }
   * }</pre>
   */
  public static void register(final ProxyServer proxy, final Object plugin) {
    final BrigadierCommand command = new BrigadierCommand(create(proxy));
    final CommandMeta meta = proxy.getCommandManager().metaBuilder(command)
        .aliases("test")
        .plugin(plugin)
        .build();

    proxy.getCommandManager().register(meta, command);
  }

  /**
   * A method for creating a Brigadier command node which denotes the declared command
   * in {@link TestCommand}. You can either retrieve the unregistered node with this method
   * or register it directly with {@link #register(ProxyServer, Object)}.
   */
  public static LiteralArgumentBuilder<CommandSource> create(final ProxyServer proxy) {
    final TestCommand instance = new TestCommand(proxy);

    return BrigadierCommand.literalArgumentBuilder("testcommand")
        .then(BrigadierCommand.literalArgumentBuilder("run")
            .executes((ctx) -> {
              if (!(ctx.getSource() instanceof Player source)) {
                throw new SimpleCommandExceptionType(
                    new LiteralMessage("This command requires a player sender!")
                ).create();
              }

              instance.run(
                  source
              );
              return 1;
            })
            .then(BrigadierCommand.requiredArgumentBuilder("target", StringArgumentType.word())
                .executes((ctx) -> {
                  instance.runWithTarget(
                      ctx.getSource(),
                      StringArgumentType.getString(ctx, "target")
                  );
                  return 1;
                })
            )
        );
  }

  /**
   * The constructor is not accessible. There is no need for an instance
   * to be created, as no state is stored, and all methods are static.
   *
   * @throws IllegalAccessException always
   */
  private TestCommandBrigadierRef() throws IllegalAccessException {
    throw new IllegalAccessException("This class cannot be instantiated.");
  }
}
