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

import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;

public sealed interface CodeStatement extends CodeVisitable {
  static VariableDeclaration variableDeclaration(CodeType type, String name, @Nullable CodeExpression assignment) {
    return new VariableDeclaration(type, name, assignment);
  }

  static ReturnStatement returnStatement(@Nullable CodeExpression returnExpression) {
    return new ReturnStatement(returnExpression);
  }

  static ThrowStatement throwStatement(CodeExpression throwExpression) {
    return new ThrowStatement(throwExpression);
  }

  static MethodInvocation methodInvocation(CodeMethod method, List<CodeExpression> parameters) {
    return new MethodInvocation(method, parameters, null);
  }

  static MethodInvocation methodInvocation(CodeMethod method, List<CodeExpression> parameters, @Nullable String instanceVariable) {
    return new MethodInvocation(method, parameters, instanceVariable);
  }

  @Override
  default <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitStatement(this);
  }

  final class VariableDeclaration implements CodeStatement {
    private final CodeType type;
    private final String name;
    private final @Nullable CodeExpression assignment;

    private VariableDeclaration(CodeType type, String name, @Nullable CodeExpression assignment) {
      this.type = type;
      this.name = name;
      this.assignment = assignment;
    }

    public CodeType type() {
      return type;
    }

    public String name() {
      return name;
    }

    @Contract(pure = true)
    public @Nullable CodeExpression assignment() {
      return assignment;
    }
  }

  final class ReturnStatement implements CodeStatement {
    private final @Nullable CodeExpression returnExpression;

    private ReturnStatement(@Nullable CodeExpression returnExpression) {
      this.returnExpression = returnExpression;
    }

    @Contract(pure = true)
    public @Nullable CodeExpression returnValue() {
      return returnExpression;
    }
  }

  final class ThrowStatement implements CodeStatement {
    private final CodeExpression throwExpression;

    private ThrowStatement(CodeExpression throwExpression) {
      this.throwExpression = throwExpression;
    }

    public CodeExpression throwExpression() {
      return throwExpression;
    }
  }

  final class MethodInvocation extends InvokesMethod implements CodeStatement {
    public MethodInvocation(CodeMethod method, List<CodeExpression> parameters, @Nullable String instanceVariable) {
      super(method, parameters, instanceVariable);
    }
  }
}
