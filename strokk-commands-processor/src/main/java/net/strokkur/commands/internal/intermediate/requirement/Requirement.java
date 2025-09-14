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
package net.strokkur.commands.internal.intermediate.requirement;

import net.strokkur.commands.internal.intermediate.ExecutorType;

import java.util.List;

public interface Requirement {

  Requirement EMPTY = (op, executorType) -> "Not handled anywhere, should not show up either.";

  static Requirement permission(String permission) {
    return new PermissionRequirement(permission);
  }

  static Requirement combine(List<Requirement> requirements) {
    return new CombinedRequirement(requirements);
  }

  static Requirement combine(Requirement... requirements) {
    return new CombinedRequirement(List.of(requirements));
  }

  //<editor-fold name="Utility Method"
  static String getDefaultRequirement(boolean operator, ExecutorType executorType) {
    if (!operator && executorType == ExecutorType.NONE) {
      return "";
    }

    if (operator && executorType == ExecutorType.NONE) {
      return "source.getSender().isOp()";
    }

    if (!operator) {
      return executorType.getPredicate();
    }

    return "source.getSender().isOp() && " + executorType.getPredicate();
  }

  String getRequirementString(boolean operator, ExecutorType executorType);
  //</editor-fold>
}
