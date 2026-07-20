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

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.exceptions.AnnotationException;
import net.strokkur.commands.internal.intermediate.executable.CommandParameter;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.UnparsedCommandParameter;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.permission.Permission;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.type.CodeType;
import net.strokkur.jap.source.annotation.AnnotationsHolder;
import net.strokkur.jap.source.classmodel.SourceParameterLike;

import java.util.List;
import java.util.Set;

public final class VelocityPlatformUtils implements PlatformUtils {
  @Override
  public void populateExecutesNode(Executable executable, CommandNode node, List<CommandParameter> parameters) {
    final SenderType type = this.getSenderType(parameters);
    executable.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
    node.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
  }

  @Override
  public CodeClassType platformType() {
    return VelocityClasses.COMMAND_SOURCE.toClassType();
  }

  @Override
  public void populateNode(AnnotationsHolder element, CommandNode node) {
    element.firstAnnotationInheritedOptional(Permission.class)
      .map(anno -> anno.value(Permission.class))
      .ifPresent(permission -> node.editAttributeMutable(
        VelocityAttributeKeys.PERMISSIONS,
        s -> s.add(permission.value()),
        () -> Set.of(permission.value())
      ));
  }

  private SenderType getSenderType(List<CommandParameter> parameters) throws AnnotationException {
    SenderType type = SenderType.NORMAL;
    for (CommandParameter parameter : parameters) {
      if (!(parameter instanceof UnparsedCommandParameter(SourceParameterLike sourceParam))) {
        continue;
      }
      final CodeType adapted = sourceParam.type().toType();

      final SenderType thisType;
      if (adapted.equals(VelocityClasses.PLAYER.toType())) {
        thisType = SenderType.PLAYER;
      } else if (adapted.equals(VelocityClasses.CONSOLE_COMMAND_SOURCE.toType())) {
        thisType = SenderType.CONSOLE;
      } else {
        thisType = type;
      }

      if (type != SenderType.NORMAL && thisType != type) {
        throw new AnnotationException("Cannot satisfy both a player and a console source.");
      }
      type = thisType;
    }

    return type;
  }

  @Override
  public InvocationChainBuilder literalBuilder(ConvertToExpression name) {
    return VelocityClasses.BRIGADIER_COMMAND
      .chainBuilder()
      .chainMethod("literalArgumentBuilder", name);
  }

  @Override
  public InvocationChainBuilder argumentBuilder(ConvertToExpression name, ConvertToExpression argument) {
    return VelocityClasses.BRIGADIER_COMMAND
      .chainBuilder()
      .chainMethod("requiredArgumentBuilder", name, argument);
  }
}
