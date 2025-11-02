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

import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.ExecutorType;
import net.strokkur.commands.internal.paper.util.PaperAttributeKeys;
import net.strokkur.commands.internal.util.MessagerWrapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class PaperTreePostProcessor extends CommonTreePostProcessor {

  PaperTreePostProcessor(final MessagerWrapper delegateMessager) {
    super(delegateMessager);
  }

  @Override
  public void cleanupPath(final CommandNode root) {
    // The relevant attributes are the 'permission', 'requires_op', 'executor, and 'requirement'
    // attributes, since these add some sort of `.requires` clause, which works the best the higher
    // up it is in the tree. Certain attribute values also cause the parent value to be written to
    // explicitly in order to **avoid** merging values. A.e. a NONE executor will force itself to
    // the parent in order to avoid being swallowed by a sister path, which may have an executor requirement.

    // Once an attribute is passed on, it will be removed from the child root in order to help the
    // source file printer a bit.
    root.forEachDepthFirst(node -> {
      handleExecutor(node);
      handleOperator(node);
      handlePermissions(node);
    });
  }

  /**
   * Here, the case is similar to the REQUIRES_OP attribute, which the slight difference that
   * the **minimum requirement** will be used, meaning if a child has an ENTITY, and another a PLAYER
   * executor requirement, the parent path will declare an ENTITY executor requirement as well.
   *
   * @param root the path to handle
   */
  private void handleExecutor(final CommandNode root) {
    ExecutorType lowestRequirement = root.hasAttribute(PaperAttributeKeys.EXECUTOR_TYPE) ? root.getAttribute(PaperAttributeKeys.EXECUTOR_TYPE) : null;
    for (final CommandNode child : root.children()) {
      final ExecutorType childExecutorType = child.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);

      if (lowestRequirement == null) {
        lowestRequirement = childExecutorType;
        continue;
      }

      if (childExecutorType == ExecutorType.NONE) {
        lowestRequirement = ExecutorType.NONE;
        break; // We cannot set an executor requirement on the parent node
      }

      if (lowestRequirement.isMoreRestrictiveOrEqualThan(childExecutorType)) {
        lowestRequirement = childExecutorType;
      }
    }

    if (lowestRequirement == ExecutorType.NONE || lowestRequirement == null) {
      root.removeAttribute(PaperAttributeKeys.EXECUTOR_TYPE);
    } else {
      root.setAttribute(PaperAttributeKeys.EXECUTOR_TYPE, lowestRequirement);
      for (final CommandNode child : root.children()) {
        if (lowestRequirement.isMoreRestrictiveOrEqualThan(child.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE))) {
          child.removeAttribute(PaperAttributeKeys.EXECUTOR_TYPE);
        }
      }
    }
  }

  private void handleOperator(final CommandNode node) {
    if (!node.hasAttribute(PaperAttributeKeys.REQUIRES_OP)) {
      final Collection<CommandNode> children = node.children();

      if (children.size() == 1) {
        if (children.stream().findFirst().get().getAttributeNotNull(PaperAttributeKeys.REQUIRES_OP)) {
          node.setAttribute(PaperAttributeKeys.REQUIRES_OP, true);
          node.forEach(child -> child.removeAttribute(PaperAttributeKeys.REQUIRES_OP));
        }
      } else if (children.size() > 1) {
        boolean allOfThemAreTrue = false;
        for (CommandNode child : children) {
          if (child.getAttributeNotNull(PaperAttributeKeys.REQUIRES_OP)) {
            allOfThemAreTrue = true;
          } else {
            allOfThemAreTrue = false;
            break;
          }
        }

        if (allOfThemAreTrue) {
          node.setAttribute(PaperAttributeKeys.REQUIRES_OP, true);
          node.forEach(child -> child.removeAttribute(PaperAttributeKeys.REQUIRES_OP));
        }
      }
    }
  }

  private void handlePermissions(final CommandNode node) {
    if (node.hasAttribute(PaperAttributeKeys.PERMISSIONS) || node.children().isEmpty()) {
      return;
    }

    final Set<String> permissions = new HashSet<>();
    for (final CommandNode child : node.children()) {
      final Set<String> childPerms = child.getAttributeNotNull(PaperAttributeKeys.PERMISSIONS);
      if (childPerms.isEmpty()) {
        // If a child doesn't have permissions, do not set any permissions for the parent
        permissions.clear();
        break;
      }

      permissions.addAll(childPerms);
    }

    // Do a second pass to clear any child permission nodes if the parent handles
    // them all exactly the same already
    for (final CommandNode child : node.children()) {
      final Set<String> childPerms = child.getAttributeNotNull(PaperAttributeKeys.PERMISSIONS);
      if (childPerms.equals(permissions)) {
        child.removeAttribute(PaperAttributeKeys.PERMISSIONS);
      }
    }

    node.setAttribute(PaperAttributeKeys.PERMISSIONS, permissions);
  }
}
