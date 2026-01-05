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
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.exceptions.AnnotationException;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.ParameterType;
import net.strokkur.commands.internal.intermediate.executable.SourceParameterType;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.permission.Permission;

import javax.net.ssl.SSLEngine;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

final class VelocityPlatformUtils implements PlatformUtils {
  @Override
  public void populateExecutesNode(final Executable executable, final CommandNode node, final List<ParameterType> parameters) {
    final SenderType type = this.getSenderType(parameters);
    executable.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
    node.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
  }

  @Override
  public String platformType() {
    return VelocityClasses.COMMAND_SOURCE;
  }

  @Override
  public void populateNode(final CommandNode node, final AnnotationsHolder element) {
    element.getAnnotationOptional(Permission.class).ifPresent(
        permission -> node.editAttributeMutable(VelocityAttributeKeys.PERMISSIONS, s -> s.add(permission.value()), () -> Set.of(permission.value()))
    );
  }

  private SenderType getSenderType(final List<ParameterType> parameters) throws AnnotationException {
    SenderType type = SenderType.NORMAL;
    for (final ParameterType parameter : parameters) {
      if (!(parameter instanceof SourceParameterType(SourceVariable sourceParam))) {
        continue;
      }

      final SenderType thisType = switch (sourceParam.getType().getFullyQualifiedName()) {
        case VelocityClasses.PLAYER -> SenderType.PLAYER;
        case VelocityClasses.CONSOLE_COMMAND_SOURCE -> SenderType.CONSOLE;
        default -> type;
      };

      if (type != SenderType.NORMAL && thisType != type) {
        throw new AnnotationException("Cannot satisfy both a player and a console source.");
      }
      type = thisType;
    }

    return type;
  }
}
