/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.as.AsBooleanExpression;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.as.AsStatement;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public sealed interface CodeExpression extends CodeVisitable, AsExpression {
  static Null nullExpr() {
    return Null.INSTANCE;
  }

  static StringLiteral string(String value) {
    return new StringLiteral(value);
  }

  static Variable variable(String name) {
    return new Variable(name);
  }

  static MethodReference methodReference(CodeType type, String methodName) {
    return new MethodReference(type, methodName);
  }

  static SingleLineLambda lambda(List<String> parameters, AsExpression expression) {
    return new SingleLineLambda(parameters, expression.getAsExpression());
  }

  static MultiLineLambda lambda(List<String> parameters, AsStatement... statements) {
    return new MultiLineLambda(
        parameters,
        Arrays.stream(statements)
            .map(AsStatement::getAsStatement)
            .toList()
    );
  }

  static Instanceof instanceofExpr(AsExpression left, CodeType.ClassType type, @Nullable String name) {
    return new Instanceof(left.getAsExpression(), type, name, false);
  }

  @Override
  default <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitExpression(this);
  }

  @Override
  default CodeExpression getAsExpression() {
    return this;
  }

  sealed abstract class BooleanExpression<S extends BooleanExpression<S>>
      implements CodeExpression, AsBooleanExpression
      permits Instanceof {
    private final boolean inverted;

    public BooleanExpression(boolean inverted) {
      this.inverted = inverted;
    }

    public boolean isInverted() {
      return inverted;
    }

    @Override
    public BooleanExpression<?> getAsBooleanExpression() {
      return this;
    }

    public abstract S invert();
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

  record MethodInvocation(InvokesMethod invokes) implements CodeExpression {
  }

  final class MethodReference implements CodeExpression {
    private final CodeType type;
    private final String methodName;

    private MethodReference(CodeType type, String methodName) {
      this.type = type;
      this.methodName = methodName;
    }

    public CodeType type() {
      return type;
    }

    public String methodName() {
      return methodName;
    }
  }

  final class SingleLineLambda implements CodeExpression {
    private final List<String> lambdaParams;
    private final CodeExpression lambdaExpression;

    private SingleLineLambda(List<String> lambdaParams, CodeExpression lambdaExpression) {
      this.lambdaParams = lambdaParams;
      this.lambdaExpression = lambdaExpression;
    }

    @Contract(pure = true)
    public @Unmodifiable List<String> lambdaParams() {
      return List.copyOf(lambdaParams);
    }

    public CodeExpression lambdaExpression() {
      return lambdaExpression;
    }
  }

  final class MultiLineLambda implements CodeExpression {
    private final List<String> lambdaParams;
    private final List<CodeStatement> statements;

    private MultiLineLambda(List<String> lambdaParams, List<CodeStatement> statements) {
      this.lambdaParams = lambdaParams;
      this.statements = statements;
    }

    @Contract(pure = true)
    public @Unmodifiable List<String> lambdaParams() {
      return List.copyOf(lambdaParams);
    }

    @Contract(pure = true)
    public @Unmodifiable List<CodeStatement> statements() {
      return List.copyOf(statements);
    }
  }

  final class Instanceof extends BooleanExpression<Instanceof> {
    private final CodeExpression left;
    private final CodeType.ClassType type;
    private final @Nullable String name;

    private Instanceof(CodeExpression left, CodeType.ClassType type, @Nullable String name, boolean inverted) {
      super(inverted);
      this.left = left;
      this.type = type;
      this.name = name;
    }

    public CodeExpression left() {
      return left;
    }

    public CodeType.ClassType type() {
      return type;
    }

    public @Nullable String name() {
      return name;
    }

    @Override
    public Instanceof invert() {
      return new Instanceof(
          left,
          type,
          name,
          !isInverted()
      );
    }
  }
}
