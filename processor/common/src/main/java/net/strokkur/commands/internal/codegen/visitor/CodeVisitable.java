package net.strokkur.commands.internal.codegen.visitor;

public interface CodeVisitable {
  <R> R accept(CodeVisitor<R> visitor);
}
