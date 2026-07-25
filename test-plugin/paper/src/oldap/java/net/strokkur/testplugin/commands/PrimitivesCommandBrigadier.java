package net.strokkur.testplugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A class holding the Brigadier source tree generated from
 * {@link PrimitivesCommand} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
 *
 * @author Strokkur24 - StrokkCommands
 * @version 2.1.3
 * @see #create() creating the LiteralCommandNode
 * @see #register(Commands) registering the command
 */
@NullMarked
public final class PrimitivesCommandBrigadier {
  public static final String NAME = "primitive";
  public static final @Nullable String DESCRIPTION = null;
  public static final List<String> ALIASES = List.of();

  /**
   * Shortcut for registering the command node returned from
   * {@link #create()}. This method uses the provided aliases
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
   *     PrimitivesCommandBrigadier.register(commands);
   * }
   * }</pre>
   */
  public static void register(final Commands commands) {
    commands.register(create(), DESCRIPTION, ALIASES);
  }

  /**
   * A method for creating a Brigadier command node which denotes the declared command
   * in {@link PrimitivesCommand}. You can either retrieve the unregistered node with this method
   * or register it directly with {@link #register(Commands)}.
   */
  public static LiteralCommandNode<CommandSourceStack> create() {
    final PrimitivesCommand instance = new PrimitivesCommand();

    return Commands.literal(NAME)
      .then(Commands.literal("bool")
        .then(Commands.argument("value", BoolArgumentType.bool())
          .executes(ctx -> {
            instance.valueType(
              ctx.getSource().getSender(),
              BoolArgumentType.getBool(ctx, "value")
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("float")
        .then(Commands.argument("value", FloatArgumentType.floatArg())
          .executes(ctx -> {
            instance.valueType(
              ctx.getSource().getSender(),
              FloatArgumentType.getFloat(ctx, "value")
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("greedy")
        .then(Commands.argument("value", StringArgumentType.greedyString())
          .executes(ctx -> {
            instance.greedyType(
              ctx.getSource().getSender(),
              StringArgumentType.getString(ctx, "value")
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("word")
        .then(Commands.argument("value", StringArgumentType.word())
          .executes(ctx -> {
            instance.wordType(
              ctx.getSource().getSender(),
              StringArgumentType.getString(ctx, "value")
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("double")
        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
          .executes(ctx -> {
            instance.valueType(
              ctx.getSource().getSender(),
              DoubleArgumentType.getDouble(ctx, "value")
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("int")
        .then(Commands.argument("value", IntegerArgumentType.integer())
          .executes(ctx -> {
            instance.valueType(
              ctx.getSource().getSender(),
              IntegerArgumentType.getInteger(ctx, "value")
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("long")
        .then(Commands.argument("value", LongArgumentType.longArg())
          .executes(ctx -> {
            instance.valueType(
              ctx.getSource().getSender(),
              LongArgumentType.getLong(ctx, "value")
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("string")
        .then(Commands.argument("value", StringArgumentType.string())
          .executes(ctx -> {
            instance.stringType(
              ctx.getSource().getSender(),
              StringArgumentType.getString(ctx, "value")
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .build();
  }

  /**
   * The constructor is not accessible. There is no need for an instance
   * to be created, as no state is stored and all methods are static.
   *
   * @throws IllegalAccessException always
   */
  private PrimitivesCommandBrigadier() throws IllegalAccessException {
    throw new IllegalAccessException("This class cannot be instantiated.");
  }
}
