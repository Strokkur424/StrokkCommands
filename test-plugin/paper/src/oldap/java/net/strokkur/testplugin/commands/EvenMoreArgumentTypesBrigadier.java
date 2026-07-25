package net.strokkur.testplugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.AxisSet;
import io.papermc.paper.command.brigadier.argument.predicate.BlockInWorldPredicate;
import io.papermc.paper.command.brigadier.argument.resolvers.AngleResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnBlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnFinePositionResolver;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A class holding the Brigadier source tree generated from
 * {@link EvenMoreArgumentTypes} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
 *
 * @author Strokkur24 - StrokkCommands
 * @version 2.1.3
 * @see #create() creating the LiteralCommandNode
 * @see #register(Commands) registering the command
 */
@NullMarked
public final class EvenMoreArgumentTypesBrigadier {
  public static final String NAME = "test-more-argument-types";
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
   *     EvenMoreArgumentTypesBrigadier.register(commands);
   * }
   * }</pre>
   */
  public static void register(final Commands commands) {
    commands.register(create(), DESCRIPTION, ALIASES);
  }

  /**
   * A method for creating a Brigadier command node which denotes the declared command
   * in {@link EvenMoreArgumentTypes}. You can either retrieve the unregistered node with this method
   * or register it directly with {@link #register(Commands)}.
   */
  public static LiteralCommandNode<CommandSourceStack> create() {
    final EvenMoreArgumentTypes instance = new EvenMoreArgumentTypes();

    return Commands.literal(NAME)
      .then(Commands.literal("block-predicate")
        .requires(source -> source.getExecutor() instanceof Player)
        .then(Commands.argument("toTest", ArgumentTypes.blockPosition())
          .then(Commands.argument("predicate", ArgumentTypes.blockInWorldPredicate())
            .executes(ctx -> {
              if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
                throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                  Component.text("This command requires a player executor!")
                )).create();
              }

              instance.blockPredicate(
                ctx.getSource().getSender(),
                executor,
                ctx.getArgument("toTest", BlockPositionResolver.class).resolve(ctx.getSource()),
                ctx.getArgument("predicate", BlockInWorldPredicate.class)
              );
              return Command.SINGLE_SUCCESS;
            })
          )
        )
      )
      .then(Commands.literal("angle")
        .then(Commands.argument("angle", ArgumentTypes.angle())
          .executes(ctx -> {
            instance.angle(
              ctx.getSource().getSender(),
              ctx.getArgument("angle", AngleResolver.class).resolve(ctx.getSource())
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("hex-color")
        .then(Commands.argument("color", ArgumentTypes.hexColor())
          .executes(ctx -> {
            instance.hexColor(
              ctx.getSource().getSender(),
              ctx.getArgument("color", TextColor.class)
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("swizzle")
        .then(Commands.argument("axes", ArgumentTypes.axes())
          .executes(ctx -> {
            instance.swizzle(
              ctx.getSource().getSender(),
              ctx.getArgument("axes", AxisSet.class)
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("column-fine")
        .then(Commands.argument("pos", ArgumentTypes.columnFinePosition())
          .executes(ctx -> {
            instance.columnFine(
              ctx.getSource().getSender(),
              ctx.getArgument("pos", ColumnFinePositionResolver.class).resolve(ctx.getSource())
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("column-block")
        .then(Commands.argument("pos", ArgumentTypes.columnBlockPosition())
          .executes(ctx -> {
            instance.columnBlock(
              ctx.getSource().getSender(),
              ctx.getArgument("pos", ColumnBlockPositionResolver.class).resolve(ctx.getSource())
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
  private EvenMoreArgumentTypesBrigadier() throws IllegalAccessException {
    throw new IllegalAccessException("This class cannot be instantiated.");
  }
}
