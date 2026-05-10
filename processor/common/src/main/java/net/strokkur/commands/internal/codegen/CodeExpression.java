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

import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;

import java.util.List;

public sealed interface CodeExpression extends CodeVisitable, AsExpression {
  static Null nullExpr() {
    return Null.INSTANCE;
  }

  static StringLiteral string(String value) {
    return new StringLiteral(value);
  }

  static ConstructorInvocation constructorCall(CodeType.ClassType type, List<? extends AsExpression> parameters) {
    return new ConstructorInvocation(type, parameters.stream()
        .map(AsExpression::getAsExpression)
        .toList()
    );
  }

  static Variable variable(String name) {
    return new Variable(name);
  }

  static MethodReference methodReference(CodeType type, String methodName) {
    return new MethodReference(type, methodName);
  }

  @Override
  default <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitExpression(this);
  }

  @Override
  default CodeExpression getAsExpression() {
    return this;
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
}
