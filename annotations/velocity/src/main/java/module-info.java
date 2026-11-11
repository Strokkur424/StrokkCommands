/// Annotations specific to the Velocity platform.
module net.strokkur.commands.velocity {
  requires static transitive net.strokkur.commands.common;
  requires static transitive net.strokkur.commands.common.permission;
  requires com.mojang.brigadier;
  requires com.velocitypowered.api;

  exports net.strokkur.commands.velocity;
  exports net.strokkur.commands.velocity.arguments;
}
