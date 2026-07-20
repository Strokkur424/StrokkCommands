import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.TreePostProcessor;
import net.strokkur.commands.internal.prototype.PrototypeNodeBuilder;
import org.jspecify.annotations.NullMarked;

@NullMarked
module net.strokkur.commands.processor.common {
  uses PlatformUtils;
  uses PrototypeNodeBuilder;
  uses TreePostProcessor;
  uses BrigadierArgumentConverter;

  requires transitive java.compiler;
  requires transitive jdk.compiler;
  requires transitive net.strokkur.commands.common;
  requires transitive net.strokkur.jap.source;
  requires static transitive org.jetbrains.annotations;
  requires static transitive org.jspecify;
  requires jdk.sctp;
  requires java.xml;
  requires java.desktop;
  requires jakarta.inject;

  exports net.strokkur.commands.internal;
  exports net.strokkur.commands.internal.arguments;
  exports net.strokkur.commands.internal.exceptions;
  exports net.strokkur.commands.internal.intermediate;
  exports net.strokkur.commands.internal.intermediate.access;
  exports net.strokkur.commands.internal.intermediate.attributes;
  exports net.strokkur.commands.internal.intermediate.registrable;
  exports net.strokkur.commands.internal.intermediate.tree;
  exports net.strokkur.commands.internal.intermediate.record;
  exports net.strokkur.commands.internal.parsing;
  exports net.strokkur.commands.internal.printer;
  exports net.strokkur.commands.internal.prototype;
  exports net.strokkur.commands.internal.util;
  exports net.strokkur.commands.internal.intermediate.executable;
}
