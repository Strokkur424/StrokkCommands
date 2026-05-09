package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public record CodeField(
    String name,
    CodeType type,
    @Nullable CodeExpression initialiser,
    Set<Modifiers> modifiers,
    List<CodeAnnotation> annotations
) implements CodeVisitable {

  @Override
  public <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitField(this);
  }
}
