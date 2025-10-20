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
package net.strokkur.commands.internal.intermediate;

import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public record TreePostProcessor(MessagerWrapper delegateMessager) implements ForwardingMessagerWrapper {

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

  public void applyDefaultExecutorPaths(final CommandNode node) {
    final DefaultExecutable defaultExecutable = node.getAttribute(AttributeKey.DEFAULT_EXECUTABLE);
    for (final CommandNode child : node.children()) {
      if (defaultExecutable != null) {
        applyDefaultExecutorPathIfUnset(child, defaultExecutable);
      } else {
        applyDefaultExecutorPaths(child);
      }
    }
  }

  private void applyDefaultExecutorPathIfUnset(final CommandNode node, final DefaultExecutable def) {
    final DefaultExecutable defaultExecutable = node.getAttribute(AttributeKey.DEFAULT_EXECUTABLE);
    for (final CommandNode child : node.children()) {
      applyDefaultExecutorPathIfUnset(child, Objects.requireNonNullElse(defaultExecutable, def));
    }
  }

  /**
   * Here, the case is similar to the REQUIRES_OP attribute, which the slight difference that
   * the **minimum requirement** will be used, meaning if a child has an ENTITY, and another a PLAYER
   * executor requirement, the parent path will declare an ENTITY executor requirement as well.
   *
   * @param root the path to handle
   */
  private void handleExecutor(final CommandNode root) {
    ExecutorType lowestRequirement = root.hasAttribute(AttributeKey.EXECUTOR_TYPE) ? root.getAttribute(AttributeKey.EXECUTOR_TYPE) : null;
    for (final CommandNode child : root.children()) {
      final ExecutorType childExecutorType = child.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);

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
      root.removeAttribute(AttributeKey.EXECUTOR_TYPE);
    } else {
      root.setAttribute(AttributeKey.EXECUTOR_TYPE, lowestRequirement);
      for (final CommandNode child : root.children()) {
        if (lowestRequirement.isMoreRestrictiveOrEqualThan(child.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE))) {
          child.removeAttribute(AttributeKey.EXECUTOR_TYPE);
        }
      }
    }
  }

  private void handleOperator(final CommandNode node) {
    if (!node.hasAttribute(AttributeKey.REQUIRES_OP)) {
      final Collection<CommandNode> children = node.children();

      if (children.size() == 1) {
        if (children.stream().findFirst().get().getAttributeNotNull(AttributeKey.REQUIRES_OP)) {
          node.setAttribute(AttributeKey.REQUIRES_OP, true);
          node.forEach(child -> child.removeAttribute(AttributeKey.REQUIRES_OP));
        }
      } else if (children.size() > 1) {
        boolean allOfThemAreTrue = false;
        for (CommandNode child : children) {
          if (child.getAttributeNotNull(AttributeKey.REQUIRES_OP)) {
            allOfThemAreTrue = true;
          } else {
            allOfThemAreTrue = false;
            break;
          }
        }

        if (allOfThemAreTrue) {
          node.setAttribute(AttributeKey.REQUIRES_OP, true);
          node.forEach(child -> child.removeAttribute(AttributeKey.REQUIRES_OP));
        }
      }
    }
  }

  private void handlePermissions(final CommandNode root) {
    if (!root.hasAttribute(AttributeKey.PERMISSIONS)) {
      final Set<String> thisPermissions = root.getAttributeNotNull(AttributeKey.PERMISSIONS);
      final Collection<CommandNode> children = root.children();

      for (final CommandNode child : root.children()) {
        if (!child.hasAttribute(AttributeKey.PERMISSIONS)) {
          // If a child doesn't have permissions, do not set any permissions for the parent
          thisPermissions.clear();
          break;
        }

        thisPermissions.addAll(child.getAttributeNotNull(AttributeKey.PERMISSIONS));
        if (children.size() == 1) {
          // We only remove the permissions if this path only has one child, meaning no cross-merging happens.
          child.removeAttribute(AttributeKey.PERMISSIONS);
        }
      }

      if (!thisPermissions.isEmpty()) {
        root.setAttribute(AttributeKey.PERMISSIONS, thisPermissions);
      }
    }
  }
}
