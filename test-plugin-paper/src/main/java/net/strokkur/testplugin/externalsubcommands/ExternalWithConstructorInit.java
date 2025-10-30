package net.strokkur.testplugin.externalsubcommands;

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import org.bukkit.command.CommandSender;

@Command("externalwithctor")
class ExternalWithConstructorInit {
  final @Subcommand MySubcommand mySub;

  ExternalWithConstructorInit(final String value) {
    this.mySub = new MySubcommand(value);
  }
}

class MySubcommand {
  private final String value;

  MySubcommand(final String value) {
    this.value = value;
  }

  @Executes
  void execute(final CommandSender sender) {
    sender.sendPlainMessage(value);
  }
}
