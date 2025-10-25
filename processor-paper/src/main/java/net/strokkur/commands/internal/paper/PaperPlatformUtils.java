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

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.ExecutorType;
import net.strokkur.commands.internal.paper.util.PaperAttributeKeys;
import net.strokkur.commands.internal.paper.util.PaperClasses;
import net.strokkur.commands.paper.Executor;
import net.strokkur.commands.paper.Permission;
import net.strokkur.commands.paper.RequiresOP;

import java.util.List;
import java.util.Set;

final class PaperPlatformUtils implements PlatformUtils {
  @Override
  public int executableFirstIndexToParse(final List<SourceParameter> parameters) {
    return getExecutorType(parameters) == ExecutorType.NONE ? 1 : 2;
  }

  @Override
  public void populateExecutesNode(final Executable executable, final CommandNode node, final List<SourceParameter> parameters) {
    final ExecutorType type = getExecutorType(parameters);
    executable.setAttribute(PaperAttributeKeys.EXECUTOR_TYPE, type);
    node.setAttribute(PaperAttributeKeys.EXECUTOR_TYPE, type);
  }

  @Override
  public String getPlatformType() {
    return PaperClasses.COMMAND_SOURCE_STACK;
  }

  @Override
  public void populateNode(final CommandNode node, final AnnotationsHolder element) {
    element.getAnnotationOptional(Permission.class).ifPresent(
        permission -> node.editAttributeMutable(PaperAttributeKeys.PERMISSIONS, s -> s.add(permission.value()), () -> Set.of(permission.value()))
    );

    if (element.hasAnnotation(RequiresOP.class)) {
      node.setAttribute(PaperAttributeKeys.REQUIRES_OP, true);
    }
  }

  private ExecutorType getExecutorType(final List<SourceParameter> parameters) {
    if (parameters.size() < 2 || parameters.get(1).getAnnotation(Executor.class) == null) {
      return ExecutorType.NONE;
    }

    return switch (parameters.get(1).getType().getFullyQualifiedName()) {
      case PaperClasses.PLAYER -> ExecutorType.PLAYER;
      case PaperClasses.ENTITY -> ExecutorType.ENTITY;
      default -> ExecutorType.NONE;
    };
  }
}
