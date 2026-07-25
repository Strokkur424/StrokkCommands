package net.strokkur.testplugin.docs.gamemode;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.kyori.adventure.text.Component;
import net.strokkur.testplugin.docs.gamemode.preset.GameModePreset;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A class holding the Brigadier source tree generated from
 * {@link GameModeCommand} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
 *
 * @author Strokkur24 - StrokkCommands
 * @version 2.1.3
 * @see #create() creating the LiteralCommandNode
 * @see #register(Commands) registering the command
 */
@NullMarked
public final class GameModeCommandBrigadier {
  public static final String NAME = "gamemode";
  public static final @Nullable String DESCRIPTION = null;
  public static final List<String> ALIASES = List.of("gm");

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
   *     GameModeCommandBrigadier.register(commands);
   * }
   * }</pre>
   */
  public static void register(final Commands commands) {
    commands.register(create(), DESCRIPTION, ALIASES);
  }

  /**
   * A method for creating a Brigadier command node which denotes the declared command
   * in {@link GameModeCommand}. You can either retrieve the unregistered node with this method
   * or register it directly with {@link #register(Commands)}.
   */
  public static LiteralCommandNode<CommandSourceStack> create() {
    final GameModeCommand instance = new GameModeCommand();
    final GameModePreset instanceS = instance.s;
    final GameModePreset instanceCreative = instance.creative;
    final GameModePreset instanceA = instance.a;
    final GameModePreset instanceSp = instance.sp;
    final GameModePreset instanceSpectator = instance.spectator;
    final GameModePreset instanceAdventure = instance.adventure;
    final GameModePreset instanceC = instance.c;
    final GameModePreset instanceSurvival = instance.survival;

    return Commands.literal(NAME)
      .requires(source -> (source.getSender().hasPermission("testplugin.gamemode.creative") || source.getSender().hasPermission("testplugin.gamemode.adventure") || source.getSender().hasPermission("testplugin.gamemode.spectator") || source.getSender().hasPermission("testplugin.gamemode.survival")))
      .then(Commands.literal("s")
        .requires(source -> source.getSender().hasPermission("testplugin.gamemode.survival"))
        .executes(ctx -> {
          if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
              Component.text("This command requires a player executor!")
            )).create();
          }

          instanceS.executes(
            ctx.getSource().getSender(),
            executor
          );
          return Command.SINGLE_SUCCESS;
        })
        .then(Commands.argument("target", ArgumentTypes.player())
          .executes(ctx -> {
            instanceS.executesTarget(
              ctx.getSource().getSender(),
              ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("creative")
        .requires(source -> source.getSender().hasPermission("testplugin.gamemode.creative"))
        .executes(ctx -> {
          if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
              Component.text("This command requires a player executor!")
            )).create();
          }

          instanceCreative.executes(
            ctx.getSource().getSender(),
            executor
          );
          return Command.SINGLE_SUCCESS;
        })
        .then(Commands.argument("target", ArgumentTypes.player())
          .executes(ctx -> {
            instanceCreative.executesTarget(
              ctx.getSource().getSender(),
              ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("a")
        .requires(source -> source.getSender().hasPermission("testplugin.gamemode.adventure"))
        .executes(ctx -> {
          if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
              Component.text("This command requires a player executor!")
            )).create();
          }

          instanceA.executes(
            ctx.getSource().getSender(),
            executor
          );
          return Command.SINGLE_SUCCESS;
        })
        .then(Commands.argument("target", ArgumentTypes.player())
          .executes(ctx -> {
            instanceA.executesTarget(
              ctx.getSource().getSender(),
              ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("sp")
        .requires(source -> source.getSender().hasPermission("testplugin.gamemode.spectator"))
        .executes(ctx -> {
          if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
              Component.text("This command requires a player executor!")
            )).create();
          }

          instanceSp.executes(
            ctx.getSource().getSender(),
            executor
          );
          return Command.SINGLE_SUCCESS;
        })
        .then(Commands.argument("target", ArgumentTypes.player())
          .executes(ctx -> {
            instanceSp.executesTarget(
              ctx.getSource().getSender(),
              ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("spectator")
        .requires(source -> source.getSender().hasPermission("testplugin.gamemode.spectator"))
        .executes(ctx -> {
          if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
              Component.text("This command requires a player executor!")
            )).create();
          }

          instanceSpectator.executes(
            ctx.getSource().getSender(),
            executor
          );
          return Command.SINGLE_SUCCESS;
        })
        .then(Commands.argument("target", ArgumentTypes.player())
          .executes(ctx -> {
            instanceSpectator.executesTarget(
              ctx.getSource().getSender(),
              ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("adventure")
        .requires(source -> source.getSender().hasPermission("testplugin.gamemode.adventure"))
        .executes(ctx -> {
          if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
              Component.text("This command requires a player executor!")
            )).create();
          }

          instanceAdventure.executes(
            ctx.getSource().getSender(),
            executor
          );
          return Command.SINGLE_SUCCESS;
        })
        .then(Commands.argument("target", ArgumentTypes.player())
          .executes(ctx -> {
            instanceAdventure.executesTarget(
              ctx.getSource().getSender(),
              ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("c")
        .requires(source -> source.getSender().hasPermission("testplugin.gamemode.creative"))
        .executes(ctx -> {
          if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
              Component.text("This command requires a player executor!")
            )).create();
          }

          instanceC.executes(
            ctx.getSource().getSender(),
            executor
          );
          return Command.SINGLE_SUCCESS;
        })
        .then(Commands.argument("target", ArgumentTypes.player())
          .executes(ctx -> {
            instanceC.executesTarget(
              ctx.getSource().getSender(),
              ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
            );
            return Command.SINGLE_SUCCESS;
          })
        )
      )
      .then(Commands.literal("survival")
        .requires(source -> source.getSender().hasPermission("testplugin.gamemode.survival"))
        .executes(ctx -> {
          if (!(ctx.getSource().getExecutor() instanceof Player executor)) {
            throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
              Component.text("This command requires a player executor!")
            )).create();
          }

          instanceSurvival.executes(
            ctx.getSource().getSender(),
            executor
          );
          return Command.SINGLE_SUCCESS;
        })
        .then(Commands.argument("target", ArgumentTypes.player())
          .executes(ctx -> {
            instanceSurvival.executesTarget(
              ctx.getSource().getSender(),
              ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst()
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
  private GameModeCommandBrigadier() throws IllegalAccessException {
    throw new IllegalAccessException("This class cannot be instantiated.");
  }
}
