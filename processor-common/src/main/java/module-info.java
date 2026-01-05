import org.jspecify.annotations.NullMarked;

@NullMarked
module net.strokkur.commands.processor.common {
  requires transitive java.compiler;
  requires transitive jdk.compiler;
  requires transitive net.strokkur.commands.common;
  requires static transitive org.jetbrains.annotations;
  requires static transitive org.jspecify;
  requires jdk.sctp;
  requires java.xml;

  exports net.strokkur.commands.internal;
  exports net.strokkur.commands.internal.abstraction;
  exports net.strokkur.commands.internal.arguments;
  exports net.strokkur.commands.internal.exceptions;
  exports net.strokkur.commands.internal.intermediate;
  exports net.strokkur.commands.internal.intermediate.access;
  exports net.strokkur.commands.internal.intermediate.attributes;
  exports net.strokkur.commands.internal.intermediate.registrable;
  exports net.strokkur.commands.internal.intermediate.tree;
  exports net.strokkur.commands.internal.parsing;
  exports net.strokkur.commands.internal.printer;
  exports net.strokkur.commands.internal.util;
  exports net.strokkur.commands.internal.intermediate.executable;
}
