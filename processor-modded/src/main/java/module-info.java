import org.jspecify.annotations.NullMarked;

@NullMarked
module net.strokkur.commands.internal.modded {
  requires transitive net.strokkur.commands.processor.common;
  requires transitive net.strokkur.commands.modded;

  exports net.strokkur.commands.internal.modded;
  exports net.strokkur.commands.internal.modded.util;
}
