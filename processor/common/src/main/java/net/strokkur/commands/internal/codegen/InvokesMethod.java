package net.strokkur.commands.internal.codegen;

import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class InvokesMethod {
  private final CodeMethod method;
  private final List<CodeExpression> parameters;
  private final @Nullable String instanceVariable;

  public InvokesMethod(CodeMethod method, List<CodeExpression> parameters, @Nullable String instanceVariable) {
    this.method = method;
    this.parameters = parameters;
    this.instanceVariable = instanceVariable;
  }

  public CodeMethod method() {
    return method;
  }

  public List<CodeExpression> parameters() {
    return parameters;
  }

  public @Nullable String instanceVariable() {
    return instanceVariable;
  }
}
