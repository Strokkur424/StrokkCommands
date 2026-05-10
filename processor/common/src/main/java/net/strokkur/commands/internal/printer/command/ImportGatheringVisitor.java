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
package net.strokkur.commands.internal.printer.command;

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeField;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeParameter;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.InvokesMethod;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImportGatheringVisitor implements CodeVisitor<Set<String>> {

  private Set<String> collectMethodInvokesImports(InvokesMethod invokes) {
    if (invokes.isCtor()) {
      return Objects.requireNonNull(invokes.type()).accept(this);
    }

    if (invokes.isStatic() && invokes.type() != null) {
      return join(
          invokes.type().accept(this),
          collect(invokes.parameters())
      );
    }
    return collect(invokes.parameters());
  }

  @SafeVarargs
  private Set<String> join(Set<String>... all) {
    return Stream.of(all)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  private Set<String> maybeAccess(@Nullable CodeVisitable visitable) {
    if (visitable != null) {
      return visitable.accept(this);
    } else {
      return Set.of();
    }
  }

  private <S extends CodeVisitable> Set<String> collect(Collection<S> collection) {
    return collection.stream()
        .flatMap(visitable -> visitable.accept(this).stream())
        .collect(Collectors.toSet());
  }

  @Override
  public Set<String> visitClass(CodeClass codeClass) {
    return join(
        Set.of(codeClass.fullyQualifiedName()),
        collect(codeClass.methods()),
        collect(codeClass.fields()),
        collect(codeClass.annotations())
    );
  }

  @Override
  public Set<String> visitMethod(CodeMethod codeMethod) {
    return join(
        collect(codeMethod.parameters()),
        collect(codeMethod.throwsExceptions()),
        codeMethod.returnType().accept(this)
    );
  }

  @Override
  public Set<String> visitPackage(CodePackage codePackage) {
    throw new IllegalStateException("This should not be called.");
  }

  @Override
  public Set<String> visitParameter(CodeParameter codeParameter) {
    return codeParameter.type().accept(this);
  }

  @Override
  public Set<String> visitType(CodeType codeType) {
    if (codeType instanceof CodeType.ArrayType array) {
      return array.inner().accept(this);
    }

    return codeType instanceof CodeType.ClassType codeClass ?
        Set.of(codeClass.fullyQualifiedName()) :
        Set.of();
  }

  @Override
  public Set<String> visitAnnotation(CodeAnnotation codeAnnotation) {
    return Set.of(codeAnnotation.type().fullyQualifiedName());
  }

  @Override
  public Set<String> visitField(CodeField codeField) {
    return join(
        codeField.initialiser() != null ? codeField.initialiser().accept(this) : Set.of(),
        collect(codeField.annotations()),
        codeField.type().accept(this)
    );
  }

  @Override
  public Set<String> visitExpression(CodeExpression codeExpression) {
    return switch (codeExpression) {
      case CodeExpression.MethodInvocation(InvokesMethod invokes) -> collectMethodInvokesImports(invokes);

      case CodeExpression.MethodReference ref -> ref.type().accept(this);

      case CodeExpression.SingleLineLambda lambda -> lambda.lambdaExpression().accept(this);

      case CodeExpression.MultiLineLambda lambda -> collect(lambda.statements());

      case CodeExpression.Instanceof instStmt -> join(
          instStmt.left().accept(this),
          instStmt.type().accept(this)
      );

      case CodeExpression.FieldAccess field -> join(
          maybeAccess(field.source()),
          field.isStatic() ? maybeAccess(field.type()) : Set.of()
      );

      default -> Set.of();
    };
  }

  @Override
  public Set<String> visitStatement(CodeStatement codeStatement) {
    return switch (codeStatement) {
      case CodeStatement.VariableDeclaration variableDeclaration -> {
        if (variableDeclaration.assignment() != null) {
          yield join(
              variableDeclaration.assignment().accept(this),
              variableDeclaration.type().accept(this)
          );
        }
        yield variableDeclaration.type().accept(this);
      }

      case CodeStatement.ReturnStatement returnStatement -> returnStatement.returnValue() != null ?
          returnStatement.returnValue().accept(this) :
          Set.of();

      case CodeStatement.ThrowStatement throwStatement -> throwStatement.throwExpression().accept(this);

      case CodeStatement.MethodInvocation(InvokesMethod invokes) -> collectMethodInvokesImports(invokes);

      case CodeStatement.If ifStmt -> join(
          ifStmt.booleanExpr().accept(this),
          collect(ifStmt.ifTrue())
      );

      case CodeStatement.Blank blank -> Set.of();
    };
  }
}
