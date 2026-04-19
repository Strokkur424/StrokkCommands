package net.strokkur.testplugin.subcommands;

import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import javax.inject.Singleton;

@Subcommand("with")
public class WithFields {
  private final MyConfigClass config;

  @Inject
  public WithFields(MyConfigClass config) {
    this.config = config;
  }

  @Executes
  void execute(CommandSender sender) {
    sender.sendRichMessage("My Value: " + config.myValue);
  }

  @Singleton
  public static final class MyConfigClass {
    public int myValue = 5;
  }
}
