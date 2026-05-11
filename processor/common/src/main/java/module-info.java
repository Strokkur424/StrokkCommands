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
  requires java.desktop;
  requires jakarta.inject;

  exports net.strokkur.commands.internal;
  exports net.strokkur.commands.internal.abstraction;
  exports net.strokkur.commands.internal.arguments;
  exports net.strokkur.commands.internal.codegen;
  exports net.strokkur.commands.internal.codegen.builder;
  exports net.strokkur.commands.internal.codegen.as;
  exports net.strokkur.commands.internal.codegen.javadoc;
  exports net.strokkur.commands.internal.codegen.visitor;
  exports net.strokkur.commands.internal.exceptions;
  exports net.strokkur.commands.internal.intermediate;
  exports net.strokkur.commands.internal.intermediate.access;
  exports net.strokkur.commands.internal.intermediate.attributes;
  exports net.strokkur.commands.internal.intermediate.registrable;
  exports net.strokkur.commands.internal.intermediate.tree;
  exports net.strokkur.commands.internal.parsing;
  exports net.strokkur.commands.internal.printer;
  exports net.strokkur.commands.internal.printer.javadoc;
  exports net.strokkur.commands.internal.printer.source;
  exports net.strokkur.commands.internal.util;
  exports net.strokkur.commands.internal.intermediate.executable;
}
