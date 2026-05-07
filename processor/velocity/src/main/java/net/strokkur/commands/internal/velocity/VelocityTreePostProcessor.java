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

import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;

import java.util.Collection;
import java.util.Set;

final class VelocityTreePostProcessor extends CommonTreePostProcessor {
  public VelocityTreePostProcessor(final MessagerWrapper delegateMessager) {
    super(delegateMessager);
  }

  @Override
  public void cleanupPath(final CommandNode root) {
    root.forEachDepthFirst(node -> {
      handleSender(node);
      handlePermissions(node);
    });
  }
  private void handleSender(final CommandNode root) {
    SenderType type = null;
    for (final CommandNode child : root.children()) {

      // If a child has no specific sender requirement, this node may not have one either
      final SenderType childType = child.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE);
      if (childType == SenderType.NORMAL) {
        root.setAttribute(VelocityAttributeKeys.SENDER_TYPE, SenderType.NORMAL);
        return;
      }

      if (type == null) {
        type = childType;
        continue;
      }

      // Two child types have different, non-normal requirements. This node needs not have a sender requirement
      if (type != childType) {
        root.setAttribute(VelocityAttributeKeys.SENDER_TYPE, SenderType.NORMAL);
        return;
      }
    }
  }

  private void handlePermissions(final CommandNode root) {
    if (!root.hasAttribute(VelocityAttributeKeys.PERMISSIONS)) {
      final Set<String> thisPermissions = root.getAttributeNotNull(VelocityAttributeKeys.PERMISSIONS);
      final Collection<CommandNode> children = root.children();

      for (final CommandNode child : root.children()) {
        if (!child.hasAttribute(VelocityAttributeKeys.PERMISSIONS)) {
          // If a child doesn't have permissions, do not set any permissions for the parent
          thisPermissions.clear();
          break;
        }

        thisPermissions.addAll(child.getAttributeNotNull(VelocityAttributeKeys.PERMISSIONS));
        if (children.size() == 1) {
          // We only remove the permissions if this path only has one child, meaning no cross-merging happens.
          child.removeAttribute(VelocityAttributeKeys.PERMISSIONS);
        }
      }

      if (!thisPermissions.isEmpty()) {
        root.setAttribute(VelocityAttributeKeys.PERMISSIONS, thisPermissions);
      }
    }
  }
}
