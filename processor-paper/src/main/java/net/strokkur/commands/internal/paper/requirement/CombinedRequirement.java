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
package net.strokkur.commands.internal.paper.requirement;

import net.strokkur.commands.internal.paper.util.ExecutorType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CombinedRequirement implements Requirement {

  private final List<Requirement> requirements;

  public CombinedRequirement(final List<Requirement> requirements) {
    this.requirements = requirements;
  }

  public static void concatPermissions(Requirement req, Set<String> handled) {
    if (req instanceof CombinedRequirement comb) {
      for (final Requirement requirement : comb.requirements) {
        concatPermissions(requirement, handled);
      }
    }

    if (req instanceof PermissionRequirement permissionRequirement) {
      handled.add(permissionRequirement.getPermission());
    }
  }

  @Override
  public String getRequirementString(boolean operator, ExecutorType executorType) {
    final Set<String> permissions = new HashSet<>();
    concatPermissions(this, permissions);

    final String defaultReq = Requirement.getDefaultRequirement(operator, executorType);
    if (permissions.isEmpty()) {
      return defaultReq;
    }

    final String permissionsString = String.join(" || ", permissions.stream()
        .map("source.getSender().hasPermission(\"%s\")"::formatted)
        .toList());

    if (defaultReq.isBlank()) {
      return permissionsString;
    }

    final String parenthesisPermissionString;
    if (permissions.size() == 1) {
      parenthesisPermissionString = permissionsString;
    } else {
      parenthesisPermissionString = "(" + permissionsString + ")";
    }

    return defaultReq + " && " + parenthesisPermissionString;
  }

  @Override
  public String toString() {
    return "CombinedRequirement{" +
        "requirements=" + requirements +
        '}';
  }
}
