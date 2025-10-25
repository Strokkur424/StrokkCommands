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
package net.strokkur.commands.internal.util;

import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;

import java.util.List;

public interface Utils {

  static String getInstanceName(List<ExecuteAccess<?>> stack) {
    final StringBuilder builder = new StringBuilder("instance");
    for (int i = 1, executeAccessStackSize = stack.size(); i < executeAccessStackSize; i++) {
      final ExecuteAccess<?> access = stack.get(i);
      final String name = access.getElement().getName();
      builder.append(name.substring(0, 1).toUpperCase()).append(name.substring(1));
    }

    return builder.toString();
  }
}
