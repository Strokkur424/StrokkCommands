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
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.permission.Permission;

import java.util.List;
import java.util.Set;

final class VelocityPlatformUtils implements PlatformUtils {
  @Override
  public void populateExecutesNode(final Executable executable, final CommandNode node, final List<SourceParameter> parameters) throws UnknownSenderException {
    final SenderType type = getSenderType(parameters);
    executable.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
    node.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
  }

  @Override
  public String getPlatformType() {
    return VelocityClasses.COMMAND_SOURCE;
  }

  @Override
  public void populateNode(final CommandNode node, final AnnotationsHolder element) {
    element.getAnnotationOptional(Permission.class).ifPresent(
        permission -> node.editAttributeMutable(VelocityAttributeKeys.PERMISSIONS, s -> s.add(permission.value()), () -> Set.of(permission.value()))
    );
  }

  private SenderType getSenderType(final List<SourceParameter> parameters) throws UnknownSenderException {
    return switch (parameters.getFirst().getType().getFullyQualifiedName()) {
      case VelocityClasses.COMMAND_SOURCE -> SenderType.NORMAL;
      case VelocityClasses.CONSOLE_COMMAND_SOURCE -> SenderType.CONSOLE;
      case VelocityClasses.PLAYER -> SenderType.PLAYER;
      default -> throw new UnknownSenderException(parameters.getFirst().getType().getSourceName() + " is not a valid command source!");
    };
  }
}
