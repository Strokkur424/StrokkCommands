package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.List;

public sealed interface CodeExpression extends CodeVisitable {
  static Null nullExpr() {
    return Null.INSTANCE;
  }

  static StringLiteral string(String value) {
    return new StringLiteral(value);
  }

  static MethodInvocation methodCall(CodeMethod method, List<CodeExpression> parameters) {
    return new MethodInvocation(method, parameters, null);
  }

  static MethodInvocation methodCall(CodeMethod method, List<CodeExpression> parameters, String instanceVariable) {
    return new MethodInvocation(method, parameters, instanceVariable);
  }

  static ConstructorInvocation constructorCall(CodeType.ClassType type, List<CodeExpression> parameters) {
    return new ConstructorInvocation(type, parameters);
  }

  static Variable variable(String name) {
    return new Variable(name);
  }

  @Override
  default <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitExpression(this);
  }

  final class StringLiteral implements CodeExpression {
    private final String value;

    private StringLiteral(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }
  }

  final class Null implements CodeExpression {
    private static final Null INSTANCE = new Null();

    private Null() {
    }
  }

  final class Variable implements CodeExpression {
    private final String name;

    private Variable(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }
  }

  final class MethodInvocation extends InvokesMethod implements CodeExpression {
    public MethodInvocation(CodeMethod method, List<CodeExpression> parameters, @Nullable String instanceVariable) {
      super(method, parameters, instanceVariable);
    }
  }

  final class ConstructorInvocation implements CodeExpression {
    private final CodeType.ClassType type;
    private final List<CodeExpression> parameters;

    private ConstructorInvocation(CodeType.ClassType type, List<CodeExpression> parameters) {
      this.type = type;
      this.parameters = parameters;
    }

    public CodeType.ClassType type() {
      return type;
    }

    public List<CodeExpression> parameters() {
      return parameters;
    }
  }
}
