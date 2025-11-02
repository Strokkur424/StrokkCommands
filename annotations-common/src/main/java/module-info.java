/// Shared annotations across all platforms.
module net.strokkur.commands.common {
  requires static com.mojang.brigadier;
  requires static org.jetbrains.annotations;
  requires static transitive org.jspecify;

  exports net.strokkur.commands;
  exports net.strokkur.commands.arguments;
  exports net.strokkur.commands.meta;
}
