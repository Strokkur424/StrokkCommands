package net.strokkur.testplugin.di;

import com.mojang.brigadier.Command;
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
 * {@link SimpleDICommand} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
 *
 * @author Strokkur24 - StrokkCommands
 * @version 2.1.3
 * @see #create(JavaPlugin, int) creating the LiteralCommandNode
 * @see #register(Commands, JavaPlugin, int) registering the command
 */
@NullMarked
public final class SimpleDICommandBrigadier {
  public static final String NAME = "simpledi";
  public static final @Nullable String DESCRIPTION = null;
  public static final List<String> ALIASES = List.of();

  /**
   * Shortcut for registering the command node returned from
   * {@link #create(JavaPlugin, int)}. This method uses the provided aliases
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
   *     SimpleDICommandBrigadier.register(commands);
   * }
   * }</pre>
   */
  public static void register(final Commands commands, final JavaPlugin plugin, final int magicValue) {
    commands.register(create(plugin, magicValue), DESCRIPTION, ALIASES);
  }

  /**
   * A method for creating a Brigadier command node which denotes the declared command
   * in {@link SimpleDICommand}. You can either retrieve the unregistered node with this method
   * or register it directly with {@link #register(Commands, JavaPlugin, int)}.
   */
  public static LiteralCommandNode<CommandSourceStack> create(final JavaPlugin plugin, final int magicValue) {
    final SimpleDICommand instance = new SimpleDICommand(plugin, magicValue);

    return Commands.literal(NAME)
      .executes(ctx -> {
        instance.execute(ctx.getSource().getSender());
        return Command.SINGLE_SUCCESS;
      })
      .build();
  }

  /**
   * The constructor is not accessible. There is no need for an instance
   * to be created, as no state is stored and all methods are static.
   *
   * @throws IllegalAccessException always
   */
  private SimpleDICommandBrigadier() throws IllegalAccessException {
    throw new IllegalAccessException("This class cannot be instantiated.");
  }
}
