package net.strokkur.testplugin.flattening;

import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;

@Command("pathless")
class PathlessSubcommands {

  @Subcommand
  First first;

  @Subcommand
  Second second;

  static class First {
    @Executes
    void execute() {
    }
  }

  static class Second {
    @Executes("sub")
    void sub() {
    }
  }
}
