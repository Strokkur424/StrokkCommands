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

class PermissionRequirement implements Requirement {

  private final String permission;

  public PermissionRequirement(final String permission) {
    this.permission = permission;
  }

  public String getPermission() {
    return permission;
  }

  @Override
  public String getRequirementString(final boolean operator, final ExecutorType executorType) {
    final String defaultReq = Requirement.getDefaultRequirement(operator, executorType);
    final String permissionString = "source.getSender().hasPermission(\"%s\")".formatted(permission);

    if (defaultReq.isEmpty()) {
      return permissionString;
    }
    return defaultReq + " && " + permissionString;
  }

  @Override
  public String toString() {
    return "PermissionRequirement{" +
        "permission='" + permission + '\'' +
        '}';
  }
}
