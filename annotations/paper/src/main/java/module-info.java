/// Annotations specific to the Paper platform.
module net.strokkur.commands.paper {
  requires static transitive net.strokkur.commands.common;
  requires static transitive net.strokkur.commands.common.permission;
  requires static org.bukkit;

  exports net.strokkur.commands.paper;
  exports net.strokkur.commands.paper.arguments;
}
