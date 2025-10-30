package net.strokkur.testplugin.flattening;

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Literal;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.meta.StrokkCommandsDebug;
import net.strokkur.commands.paper.Permission;
import org.bukkit.command.CommandSender;

@Command("permission-flattening")
@StrokkCommandsDebug(only = PermissionFlattening.class)
class PermissionFlattening {

  @Subcommand("first")
  static class First {
    @Executes("one")
    @Permission("first.one")
    void one(CommandSender sender, String top) {
    }

    @Executes("anotherone")
    @Permission("first.one")
    void anotherOne(CommandSender sender, String top) {
    }

    @Executes("two")
    @Permission("first.two")
    void two(CommandSender sender, String top) {
    }
  }

  @Permission("second.perm")
  void second(CommandSender sender, String top, @Literal String second) {
  }

  @Subcommand("third")
  static class Third {
    @Executes("one")
    @Permission("third.one")
    void one(CommandSender sender, String top) {
    }

    @Executes("anotherone")
    @Permission("third.one")
    void anotherOne(CommandSender sender, String top) {

    }
  }
}
