package net.strokkur.commands.internal.codegen;

import java.util.List;

public record CodeBlock(List<CodeStatement> statements) {
  public CodeBlock(List<CodeStatement> statements) {
    this.statements = List.copyOf(statements);
  }
}
