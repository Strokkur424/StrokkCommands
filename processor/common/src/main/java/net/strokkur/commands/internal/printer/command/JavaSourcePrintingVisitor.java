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
import net.strokkur.commands.internal.codegen.CodeConstructor;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeField;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodeParameter;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.InvokesMethod;
import net.strokkur.commands.internal.codegen.Modifiers;
import net.strokkur.commands.internal.printer.javadoc.AbstractJavadocPrintingVisitor;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class JavaSourcePrintingVisitor extends AbstractSourcePrintingVisitor {
  public JavaSourcePrintingVisitor(Supplier<AbstractJavadocPrintingVisitor> javadocPrintingVisitor, String indentString) {
    super(javadocPrintingVisitor, indentString);
  }

  @Override
  public StringBuilder visitClass(CodeClass codeClass) {
    class ClassPrintUtil {
      private void printMethods(StringBuilder builder, List<CodeMethod> methods) {
        methods.forEach(method -> {
          builder.append("\n");
          appendNested(builder, method);
        });
      }
    }

    final ClassPrintUtil util = new ClassPrintUtil();
    return append(builder -> {
      printJavadocIndented(builder, codeClass.javadoc());
      printAnnotationsIndented(builder, codeClass.annotations());
      printModifiersIndented(builder, codeClass.modifiers());
      builder.append("class ");
      builder.append(codeClass.name());
      builder.append(" {\n");

      appendIndented(() -> {
        codeClass.fields().forEach(field -> appendNested(builder, field));

        final List<CodeMethod> staticMethods = codeClass.methods().stream()
            .filter(method -> method.modifiers().contains(Modifiers.STATIC))
            .toList();
        util.printMethods(builder, staticMethods);

        final List<CodeMethod> constructors = codeClass.methods().stream()
            .filter(CodeConstructor.class::isInstance)
            .toList();
        util.printMethods(builder, constructors);

        final List<CodeMethod> instanceMethods = codeClass.methods().stream()
            .filter(Predicate.not(method -> method.modifiers().contains(Modifiers.STATIC)))
            .filter(Predicate.not(CodeConstructor.class::isInstance))
            .toList();
        util.printMethods(builder, instanceMethods);
      });

      appendIndent(builder);
      builder.append("}\n");
    });
  }

  @Override
  public StringBuilder visitMethod(CodeMethod codeMethod) {
    return append(builder -> {
      printJavadocIndented(builder, codeMethod.javadoc());
      printModifiersIndented(builder, codeMethod.modifiers());
      builder.append(codeMethod.returnType().accept(this));
      if (!(codeMethod instanceof CodeConstructor)) {
        builder.append(" ");
        builder.append(codeMethod.name());
      }

      builder.append("(");
      builder.append(joining(codeMethod.parameters()));
      builder.append(")");

      if (!codeMethod.throwsExceptions().isEmpty()) {
        builder.append(" throws ");
        builder.append(joining(codeMethod.throwsExceptions()));
      }

      builder.append(" {\n");
      appendIndented(() -> codeMethod.codeBlock().statements().forEach(
          stmt -> appendNested(builder, stmt)
      ));
      appendIndent(builder);
      builder.append("}\n");
    });
  }

  @Override
  public StringBuilder visitParameter(CodeParameter codeParameter) {
    return append(builder -> {
      appendNested(builder, codeParameter.type());
      builder.append(" ");
      builder.append(codeParameter.name());
    });
  }

  @Override
  public StringBuilder visitType(CodeType codeType) {
    return new StringBuilder(codeType.name());
  }

  @Override
  public StringBuilder visitAnnotation(CodeAnnotation codeAnnotation) {
    return append(builder -> {
      builder.append("@");
      appendNested(builder, codeAnnotation.type());
    });
  }

  @Override
  public StringBuilder visitField(CodeField codeField) {
    return append(builder -> {
      printModifiersIndented(builder, codeField.modifiers());
      appendNested(builder, codeField.type());
      builder.append(" ");
      builder.append(codeField.name());
      if (codeField.initialiser() != null) {
        builder.append(" = ");
        appendNested(builder, codeField.initialiser());
      }
      builder.append(";\n");
    });
  }

  @Override
  public StringBuilder visitExpression(CodeExpression codeExpression) {
    return append(builder -> {
      switch (codeExpression) {
        case CodeExpression.MethodInvocation(InvokesMethod invokes) -> {
          appendMethodInvocation(builder, invokes);
        }
        case CodeExpression.Null ignored -> {
          builder.append("null");
        }
        case CodeExpression.StringLiteral stringLiteral -> {
          builder.append('"').append(stringLiteral.value()).append('"');
        }
        case CodeExpression.Variable variable -> {
          builder.append(variable.name());
        }
        case CodeExpression.MethodReference ref -> {
          builder.append(ref.type().name()).append("::").append(ref.methodName());
        }
        case CodeExpression.SingleLineLambda lambda -> {
          appendLambdaHead(builder, lambda.lambdaParams());
          appendNested(builder, lambda.lambdaExpression());
        }
        case CodeExpression.MultiLineLambda lambda -> {
          appendLambdaHead(builder, lambda.lambdaParams());
          builder.append("{\n");
          appendIndented(() -> {
            for (CodeStatement statement : lambda.statements()) {
              appendNested(builder, statement);
            }
          });
          appendIndent(builder);
          builder.append("}");
        }
        case CodeExpression.Instanceof instExpr -> {
          if (instExpr.isInverted()) {
            builder.append("!(");
          }
          appendNested(builder, instExpr.left());
          builder.append(" instanceof ");
          builder.append(instExpr.type().name());
          if (instExpr.name() != null) {
            builder.append(" ").append(instExpr.name());
          }
          if (instExpr.isInverted()) {
            builder.append(")");
          }
        }
        case CodeExpression.FieldAccess(
            CodeType.ClassType type, CodeExpression source, String fieldName, boolean isStatic
        ) -> {
          if (source != null) {
            appendNested(builder, source);
            builder.append(".");
          } else if (type != null && isStatic) {
            builder.append(type.name());
            builder.append(".");
          }

          builder.append(fieldName);
        }
        default -> throw new IllegalStateException("Invalid expression: " + codeExpression.getClass().getName());
      }
    });
  }

  @Override
  public StringBuilder visitStatement(CodeStatement codeStatement) {
    return append(builder -> {
      if (codeStatement instanceof CodeStatement.Blank) {
        builder.append("\n");
        return;
      }

      appendIndent(builder);

      if (codeStatement instanceof CodeStatement.If ifStmt) {
        builder.append("if (");
        appendNested(builder, ifStmt.booleanExpr());
        builder.append(") {\n");
        appendIndented(() -> {
          ifStmt.ifTrue().forEach(stmt -> appendNested(builder, stmt));
        });
        appendIndent(builder);
        builder.append("}\n");
        return;
      }

      switch (codeStatement) {
        case CodeStatement.MethodInvocation(InvokesMethod invokes) -> {
          appendMethodInvocation(builder, invokes);
        }
        case CodeStatement.ReturnStatement returnStatement -> {
          builder.append("return");
          if (returnStatement.returnValue() != null) {
            builder.append(" ");
            appendNested(builder, returnStatement.returnValue());
          }
        }
        case CodeStatement.ThrowStatement throwStatement -> {
          builder.append("throw ");
          appendNested(builder, throwStatement.throwExpression());
        }
        case CodeStatement.VariableDeclaration variableDeclaration -> {
          if (variableDeclaration.isFinal()) {
            builder.append("final ");
          }
          appendNested(builder, variableDeclaration.type());
          builder.append(" ");
          builder.append(variableDeclaration.name());
          if (variableDeclaration.assignment() != null) {
            builder.append(" = ");
            appendNested(builder, variableDeclaration.assignment());
          }
        }
        default -> throw new IllegalStateException("Invalid statement: " + codeStatement.getClass().getName());
      }
      builder.append(";\n");
    });
  }

  private void appendLambdaHead(StringBuilder builder, List<String> lambdaParams) {
    if (lambdaParams.size() != 1) {
      builder.append("(");
      builder.append(String.join(", ", lambdaParams));
      builder.append(")");
    } else {
      builder.append(lambdaParams.getFirst());
    }
    builder.append(" -> ");
  }
}
