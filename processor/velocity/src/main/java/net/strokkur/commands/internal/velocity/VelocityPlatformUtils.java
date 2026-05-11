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
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.adapter.CodeTypeAdapter;
import net.strokkur.commands.internal.exceptions.AnnotationException;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.ParameterType;
import net.strokkur.commands.internal.intermediate.executable.SourceParameterType;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.permission.Permission;

import java.util.List;
import java.util.Set;

final class VelocityPlatformUtils implements PlatformUtils {
  @Override
  public void populateExecutesNode(Executable executable, CommandNode node, List<ParameterType> parameters) {
    final SenderType type = this.getSenderType(parameters);
    executable.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
    node.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
  }

  @Override
  public String platformType() {
    return VelocityClasses.COMMAND_SOURCE.getAsCodeType().fullyQualifiedName();
  }

  @Override
  public void populateNode(CommandNode node, AnnotationsHolder element) {
    element.getAnnotationOptional(Permission.class).ifPresent(
        permission -> node.editAttributeMutable(VelocityAttributeKeys.PERMISSIONS, s -> s.add(permission.value()), () -> Set.of(permission.value()))
    );
  }

  private SenderType getSenderType(List<ParameterType> parameters) throws AnnotationException {
    SenderType type = SenderType.NORMAL;
    for (ParameterType parameter : parameters) {
      if (!(parameter instanceof SourceParameterType(SourceVariable sourceParam))) {
        continue;
      }
      final CodeType adapted = CodeTypeAdapter.from(sourceParam.getType());

      final SenderType thisType;
      if (adapted.equals(VelocityClasses.PLAYER.getAsCodeType())) {
        thisType = SenderType.PLAYER;
      } else if (adapted.equals(VelocityClasses.CONSOLE_COMMAND_SOURCE.getAsCodeType())) {
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
}
