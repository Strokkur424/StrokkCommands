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
import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImportGatheringVisitor implements CodeVisitor<Set<String>> {

  @SafeVarargs
  private Set<String> join(Set<String>... all) {
    return Stream.of(all)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
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
    return Set.of();
  }

  @Override
  public Set<String> visitParameter(CodeParameter codeParameter) {
    return codeParameter.type().accept(this);
  }

  @Override
  public Set<String> visitType(CodeType codeType) {
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
      case CodeExpression.MethodInvocation methodInvocation -> {
        if (methodInvocation.instanceVariable() == null) {
          yield join(
              methodInvocation.method().declaredClass().accept(this),
              collect(methodInvocation.parameters())
          );
        }
        yield collect(methodInvocation.parameters());
      }

      case CodeExpression.ConstructorInvocation ctorInvocation -> join(
          ctorInvocation.type().accept(this),
          collect(ctorInvocation.parameters())
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

      case CodeStatement.MethodInvocation methodInvocation -> {
        if (methodInvocation.instanceVariable() == null) {
          yield join(
              methodInvocation.method().declaredClass().accept(this),
              collect(methodInvocation.parameters())
          );
        }
        yield collect(methodInvocation.parameters());
      }

      default -> Set.of();
    };
  }
}
