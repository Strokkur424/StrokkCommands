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
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.exceptions.AnnotationException;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.executable.CommandParameter;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.UnparsedCommandParameter;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.ExecutorType;
import net.strokkur.commands.internal.paper.util.PaperAttributeKeys;
import net.strokkur.commands.internal.paper.util.PaperClasses;
import net.strokkur.commands.paper.Executor;
import net.strokkur.commands.paper.RequiresOP;
import net.strokkur.commands.permission.Permission;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.source.annotation.AnnotationsHolder;
import net.strokkur.jap.source.classmodel.SourceParameterLike;

import java.util.HashSet;
import java.util.List;

@AutoService(PlatformUtils.class)
public final class PaperPlatformUtils implements PlatformUtils {

  @Override
  public void populateExecutesNode(Executable executable, CommandNode node, List<CommandParameter> parameters) throws UnknownSenderException {
    final ExecutorType type = getExecutorType(parameters);
    executable.setAttribute(PaperAttributeKeys.EXECUTOR_TYPE, type);
    node.setAttribute(PaperAttributeKeys.EXECUTOR_TYPE, type);
  }

  @Override
  public void populateNode(AnnotationsHolder holder, CommandNode node) {
    final List<String> permission = holder.annotationsValuesInherited(Permission.class).stream()
      .map(Permission::value)
      .toList();

    if (!permission.isEmpty()) {
      node.forEachChildElseSelf(n -> n.editAttributeMutable(
        PaperAttributeKeys.PERMISSIONS,
        s -> s.addAll(permission),
        () -> new HashSet<>(permission)
      ));
    }

    if (holder.hasAnnotationInherited(RequiresOP.class)) {
      node.forEachChildElseSelf(n -> n.setAttribute(PaperAttributeKeys.REQUIRES_OP, true));
    }
  }

  @Override
  public boolean mayParameterBeArgument(SourceParameterLike param) {
    return !param.hasAnnotation(Executor.class);
  }

  @Override
  public CodeClassType platformType() {
    return PaperClasses.COMMAND_SOURCE_STACK.toClassType();
  }

  @Override
  public InvocationChainBuilder literalBuilder(ConvertToExpression name) {
    return PaperClasses.COMMANDS.chainBuilder()
      .chainMethod("literal", name);
  }

  @Override
  public InvocationChainBuilder argumentBuilder(ConvertToExpression name, ConvertToExpression argument) {
    return PaperClasses.COMMANDS.chainBuilder()
      .chainMethod("argument", name, argument);
  }

  //
  // Utils
  //

  private ExecutorType getExecutorType(List<CommandParameter> parameters) throws AnnotationException {
    ExecutorType type = ExecutorType.NONE;
    for (CommandParameter parameter : parameters) {
      if (!(parameter instanceof UnparsedCommandParameter(SourceParameterLike param))) {
        continue;
      }

      if (!param.hasAnnotationInherited(Executor.class)) {
        continue;
      }

      if (param.type().isType(PaperClasses.PLAYER)) {
        return ExecutorType.PLAYER;
      }

      if (param.type().isType(PaperClasses.ENTITY)) {
        type = ExecutorType.ENTITY;
        continue;
      }

      throw new AnnotationException("Illegal class annotated with @Executor", param);
    }

    return type;
  }
}
