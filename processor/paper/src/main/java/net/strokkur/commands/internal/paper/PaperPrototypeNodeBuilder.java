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
package net.strokkur.commands.internal.paper;

import com.google.auto.service.AutoService;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.ExecutorType;
import net.strokkur.commands.internal.paper.util.PaperAttributeKeys;
import net.strokkur.commands.internal.paper.util.PaperClasses;
import net.strokkur.commands.internal.prototype.PrototypeNode;
import net.strokkur.commands.internal.prototype.PrototypeNodeBuilder;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.paper.Executor;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToStatement;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.statement.Statements;
import net.strokkur.jap.code.util.StyleConfig;
import net.strokkur.jap.source.classmodel.SourceParameterLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@AutoService(PrototypeNodeBuilder.class)
public final class PaperPrototypeNodeBuilder extends PrototypeNodeBuilder {

  @Override
  protected void handleAttributes(PrototypeNode prototype, CommandNode node) {
    super.handleAttributes(prototype, node);

    final List<ConvertToExpression> extraRequirements = new ArrayList<>();

    final ExecutorType executorType = node.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);
    if (executorType != ExecutorType.NONE) {
      extraRequirements.add(Expressions.variable("source").chainMethod("getExecutor").instanceOf(executorType.classType()));
    }

    final boolean operator = node.getAttributeNotNull(PaperAttributeKeys.REQUIRES_OP);
    if (operator) {
      extraRequirements.add(Expressions.variable("source").chainMethod("getSender").chainMethod("isOp"));
    }

    node.getAttributeNotNull(PaperAttributeKeys.PERMISSIONS).stream()
      .map(perm -> (ConvertToExpression) Expressions.variable("source").chainMethod("getSender").chainMethod("hasPermission", Expressions.string(perm)))
      .reduce(ConvertToExpression::or)
      .ifPresent(extraRequirements::add);

    final Optional<ConvertToExpression> requirement = extraRequirements.stream()
      .reduce(ConvertToExpression::and);

    requirement.ifPresent(req -> {
      if (prototype.requires == null) {
        prototype.requires = () -> req;
      } else {
        final ConvertToExpression and = prototype.requires.requirementExpression().and(req);
        prototype.requires = () -> and;
      }
    });
  }

  @Override
  protected List<? extends ConvertToStatement> validationStatements(Executable executable) {
    final ExecutorType executorType = executable.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);
    if (executorType != ExecutorType.NONE) {
      return List.of(
        Statements.ifStmt(
          Expressions.variable("ctx").chainMethod("getSource").chainMethod("getExecutor")
            .instanceOf(executorType.classType(), "executor")
            .not(),
          Classes.SIMPLE_COMMAND_EXCEPTION_TYPE.ctor(
            Classes.LITERAL_MESSAGE.ctor(Expressions.string("This command requires %s %s executor!".formatted(
              executorType == ExecutorType.ENTITY ? "an" : "a",
              executorType.classType().toClassType().simpleName()
            )))
          ).setStyle(StyleConfig.MULTILINE).chainMethod("create").throwStmt()
        ),
        Statements.blank()
      );
    }

    return List.of();
  }

  @Override
  protected ConvertToExpression convertUnparsedParameter(SourceParameterLike parameter) {
    if (parameter.hasAnnotationInherited(Executor.class)) {
      return Expressions.variable("executor");
    }

    if (parameter.type().isType(Classes.COMMAND_CONTEXT.typed(PaperClasses.COMMAND_SOURCE_STACK))) {
      return Expressions.variable("ctx");
    }

    if (parameter.type().isType(PaperClasses.COMMAND_SOURCE_STACK)) {
      return Expressions.variable("ctx").chainMethod("getSource");
    }

    if (parameter.type().isType(PaperClasses.COMMAND_SENDER)) {
      return Expressions.variable("ctx").chainMethod("getSource").chainMethod("getSender");
    }

    final DefaultExecutable.Type type = DefaultExecutable.Type.getType(parameter.type());
    if (type == DefaultExecutable.Type.LIST || type == DefaultExecutable.Type.ARRAY) {
      return Objects.requireNonNull(type.getter());
    }

    throw new IllegalStateException("Unknown parameter type: " + parameter.type().toType().fullyQualifiedName());
  }
}
