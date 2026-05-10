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
import net.strokkur.commands.internal.codegen.as.AsStatement;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface CodeStatement extends CodeVisitable, AsStatement {
  static VariableDeclaration variableDeclaration(CodeType type, String name, @Nullable AsExpression assignment) {
    return new VariableDeclaration(type, name, assignment == null ? null : assignment.getAsExpression(), false);
  }

  static VariableDeclaration variableDeclarationFinal(CodeType type, String name, @Nullable AsExpression assignment) {
    return new VariableDeclaration(type, name, assignment == null ? null : assignment.getAsExpression(), true);
  }

  static ReturnStatement returnStatement(@Nullable AsExpression returnExpression) {
    return new ReturnStatement(returnExpression == null ? null : returnExpression.getAsExpression());
  }

  static ThrowStatement throwStatement(CodeExpression throwExpression) {
    return new ThrowStatement(throwExpression);
  }

  static Blank blank() {
    return Blank.INSTANCE;
  }

  @Override
  default CodeStatement getAsStatement() {
    return this;
  }

  @Override
  default <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitStatement(this);
  }

  final class VariableDeclaration implements CodeStatement {
    private final CodeType type;
    private final String name;
    private final @Nullable CodeExpression assignment;
    private final boolean isFinal;

    private VariableDeclaration(CodeType type, String name, @Nullable CodeExpression assignment, boolean isFinal) {
      this.type = type;
      this.name = name;
      this.assignment = assignment;
      this.isFinal = isFinal;
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

    public boolean isFinal() {
      return isFinal;
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

  record MethodInvocation(InvokesMethod invokes) implements CodeStatement {
  }

  final class Blank implements CodeStatement {
    private static final Blank INSTANCE = new Blank();

    private Blank() {
    }
  }
}
