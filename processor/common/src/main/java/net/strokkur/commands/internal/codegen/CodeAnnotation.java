package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;

public record CodeAnnotation(CodeType.ClassType type) implements CodeVisitable {
  public static CodeAnnotation of(CodeType.ClassType type) {
    return new CodeAnnotation(type);
  }

  @Override
  public <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitAnnotation(this);
  }
}
