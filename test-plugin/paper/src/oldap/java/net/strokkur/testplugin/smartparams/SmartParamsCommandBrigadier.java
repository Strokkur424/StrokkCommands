package net.strokkur.testplugin.smartparams;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

/**
 * A class holding the Brigadier source tree generated from
 * {@link SmartParamsCommand} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
 *
 * @author Strokkur24 - StrokkCommands
 * @version 2.1.3
 * @see #create(Logger) creating the LiteralCommandNode
 * @see #register(Commands, Logger) registering the command
 */
@NullMarked
public final class SmartParamsCommandBrigadier {
  public static final String NAME = "smartparams";
  public static final @Nullable String DESCRIPTION = null;
  public static final List<String> ALIASES = List.of();

  /**
   * Shortcut for registering the command node returned from
   * {@link #create(Logger)}. This method uses the provided aliases
   * and description from the original source file.
   * <p>
   * <h3>Registering the command</h3>
   * <p>
   * This method can safely be called either in your plugin bootstrapper's
   * {@link PluginBootstrap#bootstrap(BootstrapContext)}, your main
   * class' {@link JavaPlugin#onLoad()} or {@link JavaPlugin#onEnable()}
   * method.
   * <p>
   * You need to call it inside of a lifecycle event. General information can be found on the
   * <a href="https://docs.papermc.io/paper/dev/lifecycle/">PaperMC Lifecycle API docs page</a>.
   * The general use case might look like this (example given inside the {@code onEnable} method):
   * <p>
   * <pre>{@code
   * this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
   *     final Commands commands = event.registrar();
   *     SmartParamsCommandBrigadier.register(commands);
   * }
   * }</pre>
   */
  public static void register(final Commands commands, final Logger logger) {
    commands.register(create(logger), DESCRIPTION, ALIASES);
  }

  /**
   * A method for creating a Brigadier command node which denotes the declared command
   * in {@link SmartParamsCommand}. You can either retrieve the unregistered node with this method
   * or register it directly with {@link #register(Commands, Logger)}.
   */
  public static LiteralCommandNode<CommandSourceStack> create(final Logger logger) {
    final SmartParamsCommand instance = new SmartParamsCommand(logger);

    return Commands.literal(NAME)
      .then(Commands.literal("no-params")
        .executes(ctx -> {
          instance.noParameters();
          return Command.SINGLE_SUCCESS;
        })
      )
      .then(Commands.literal("between-arg")
        .requires(source -> source.getExecutor() instanceof Player)
        .then(Commands.argument("wordArg", StringArgumentType.word())
          .then(Commands.argument("value", IntegerArgumentType.integer())
            .executes(ctx -> {
              if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
                throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                  Component.text("This command requires a player executor!")
                )).create();
              }

              instance.betweenArg(
                StringArgumentType.getString(ctx, "wordArg"),
                ctx.getSource().getSender(),
                executor,
                IntegerArgumentType.getInteger(ctx, "value"),
                ctx.getInput().split(" ")
              );
              return Command.SINGLE_SUCCESS;
            })
          )
        )
      )
      .then(Commands.literal("involving-multi-args")
        .then(Commands.literal("third")
          .executes(ctx -> {
            instance.multipleArgs(
              ctx.getSource(),
              "third"
            );
            return Command.SINGLE_SUCCESS;
          })
        )
        .then(Commands.literal("first")
          .executes(ctx -> {
            instance.multipleArgs(
              ctx.getSource(),
              "first"
            );
            return Command.SINGLE_SUCCESS;
          })
        )
        .then(Commands.literal("second")
          .executes(ctx -> {
            instance.multipleArgs(
              ctx.getSource(),
              "second"
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("all-of-them")
        .executes(ctx -> {
          instance.allOfEm(
            ctx,
            ctx.getSource(),
            ctx.getSource().getSender()
          );
          return Command.SINGLE_SUCCESS;
        })
      )
      .then(Commands.literal("source")
        .executes(ctx -> {
          instance.wholeSourceStack(ctx.getSource());
          return Command.SINGLE_SUCCESS;
        })
      )
      .build();
  }

  /**
   * The constructor is not accessible. There is no need for an instance
   * to be created, as no state is stored and all methods are static.
   *
   * @throws IllegalAccessException always
   */
  private SmartParamsCommandBrigadier() throws IllegalAccessException {
    throw new IllegalAccessException("This class cannot be instantiated.");
  }
}
