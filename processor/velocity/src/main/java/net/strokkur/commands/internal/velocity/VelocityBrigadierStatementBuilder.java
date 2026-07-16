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

import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.printer.CommonBrigadierStatementBuilder;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToStatement;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;
import net.strokkur.jap.code.statement.Statements;
import net.strokkur.jap.code.type.CodeType;
import net.strokkur.jap.code.util.StyleConfig;
import net.strokkur.jap.source.classmodel.SourceParameterLike;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

class VelocityBrigadierStatementBuilder extends CommonBrigadierStatementBuilder {

  @Override
  protected InvocationChainBuilder literalBuilder(ConvertToExpression name) {
    return VelocityClasses.BRIGADIER_COMMAND
      .chainBuilder()
      .chainMethod("literalArgumentBuilder", name);
  }

  @Override
  protected InvocationChainBuilder argumentBuilder(ConvertToExpression name, ConvertToExpression argument) {
    return VelocityClasses.BRIGADIER_COMMAND
      .chainBuilder()
      .chainMethod("requiredArgumentBuilder", name, argument);
  }

  @Override
  protected List<? extends ConvertToStatement> validationStatements(Executable executable) {
    final SenderType type = executable.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE);
    if (type != SenderType.NORMAL) {
      return List.of(
        Statements.ifStmt(
          Expressions.variable("ctx").chainMethod("getSource").instanceOf(
            type.getClassType(),
            "source"
          ).not(),
          Classes.SIMPLE_COMMAND_EXCEPTION_TYPE.ctor()
            .addParameters(Classes.LITERAL_MESSAGE.ctor(
              Expressions.string("This command requires a %s sender!".formatted(type.getClassType().toType().simpleName().toLowerCase(Locale.ROOT)))
            ))
            .setStyle(StyleConfig.MULTILINE)
            .chainMethod("create")
            .throwStmt()
        ),
        Statements.blank()
      );
    }

    return List.of();
  }

  @Override
  protected ConvertToExpression convertUnparsedParameter(SourceParameterLike parameter) {
    final CodeType paramType = parameter.type().toType();

    if (Classes.COMMAND_CONTEXT.typed(VelocityClasses.COMMAND_SOURCE).equals(paramType)) {
      return Expressions.variable("ctx");
    }

    if (VelocityClasses.COMMAND_SOURCE.toType().equals(paramType)) {
      return Expressions.variable("ctx").chainMethod("getSource");
    }

    if (VelocityClasses.PLAYER.toType().equals(paramType) || VelocityClasses.CONSOLE_COMMAND_SOURCE.toType().equals(paramType)) {
      return Expressions.variable("source");
    }

    final DefaultExecutable.Type type = DefaultExecutable.Type.getType(parameter.type());
    if (type == DefaultExecutable.Type.LIST || type == DefaultExecutable.Type.ARRAY) {
      return Objects.requireNonNull(type.getter());
    }

    throw new IllegalStateException("Unknown parameter type: " + parameter.type());
  }
}
