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
package net.strokkur.commands.internal.velocity;

import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.as.AsStatement;
import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.codegen.builder.MethodInvocationBuilder;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.printer.CommonBrigadierStatementBuilder;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;

import java.util.List;
import java.util.Objects;

class VelocityBrigadierStatementBuilder extends CommonBrigadierStatementBuilder {
  @Override
  protected MethodInvocationBuilder literalBuilder(AsExpression name) {
    return Builders.methodInvocation("literalArgumentBuilder").setStatic(VelocityClasses.BRIGADIER_COMMAND)
        .addParameter(name);
  }

  @Override
  protected MethodInvocationBuilder argumentBuilder(AsExpression name, AsExpression argument) {
    return Builders.methodInvocation("requiredArgumentBuilder").setStatic(VelocityClasses.BRIGADIER_COMMAND)
        .addParameter(name)
        .addParameter(argument);
  }

  @Override
  protected List<AsStatement> validationStatements(Executable executable) {
    final SenderType type = executable.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE);
    if (type != SenderType.NORMAL) {
      return List.of(
          CodeStatement.ifStmt(
              CodeExpression.instanceofExpr(
                  Builders.methodInvocation("getSource").setInstanceVariable("ctx"),
                  type.getClassType().getAsCodeType(),
                  "source"
              ).invert(),
              CodeStatement.throwStatement(Builders.ctorInvocation(Classes.SIMPLE_COMMAND_EXCEPTION_TYPE)
                  .setMultilineParameters()
                  .addParameter(Builders.ctorInvocation(Classes.LITERAL_MESSAGE).addParameter(CodeExpression.string(
                      "This command requires a %s sender!".formatted(type.getClassType().getAsCodeType().name().toLowerCase())
                  )))
                  .chain("create")
              )
          ),
          CodeStatement.blank()
      );
    }

    return List.of();
  }

  @Override
  protected AsExpression getParameterValueExpr(SourceVariable parameter) {
    if (parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(Classes.COMMAND_CONTEXT.getAsCodeType().fullyQualifiedName() + "<" + VelocityClasses.COMMAND_SOURCE.getAsCodeType().fullyQualifiedName() + ">")) {
      return CodeExpression.variable("ctx");
    }

    if (parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(VelocityClasses.COMMAND_SOURCE.getAsCodeType().fullyQualifiedName())) {
      return Builders.methodInvocation("getSource").setInstanceVariable("ctx");
    }

    if (parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(VelocityClasses.PLAYER.getAsCodeType().fullyQualifiedName())
        || parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(VelocityClasses.CONSOLE_COMMAND_SOURCE.getAsCodeType().fullyQualifiedName())) {
      return CodeExpression.variable("source");
    }

    final DefaultExecutable.Type type = DefaultExecutable.Type.getType(parameter);
    if (type == DefaultExecutable.Type.LIST || type == DefaultExecutable.Type.ARRAY) {
      return Objects.requireNonNull(type.getGetter());
    }

    throw new IllegalStateException("Unknown parameter type: " + parameter.getFullDefinition());
  }
}
