package net.strokkur.testplugin.di;

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;

@Command("complex-di")
class ComplexDICommand<T extends JavaPlugin> {
  private final T plugin;

  public <S extends Supplier<T>> ComplexDICommand(final S pluginSupplier) {
    this.plugin = pluginSupplier.get();
  }

  @Executes
  void execute(CommandSender sender) {
    sender.sendPlainMessage("Found " + plugin.getName());
  }
}






