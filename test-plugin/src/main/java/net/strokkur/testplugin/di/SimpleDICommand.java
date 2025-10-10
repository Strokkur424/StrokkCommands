package net.strokkur.testplugin.di;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@Command("simpledi")
class SimpleDICommand {
  private final JavaPlugin plugin;
  private final int magicValue;

  public SimpleDICommand(final JavaPlugin plugin, final int magicValue) {
    this.plugin = plugin;
    this.magicValue = magicValue;
  }

  @Executes
  void execute(CommandSender sender) {
    this.plugin.getSLF4JLogger().info("Magic value: {}", magicValue);
  }
}
