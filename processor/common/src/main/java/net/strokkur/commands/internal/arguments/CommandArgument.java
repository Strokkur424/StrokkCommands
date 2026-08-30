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
package net.strokkur.commands.internal.arguments;

import net.strokkur.commands.internal.intermediate.executable.CommandParameter;

import java.util.ArrayList;
import java.util.List;

public non-sealed interface CommandArgument extends CommandParameter {
  String argumentName();

  /// Filters a list of [CommandParameter] into one with only [CommandArgument] instances.
  static List<CommandArgument> argsFromParameters(List<CommandParameter> in) {
    return in.stream()
      .filter(CommandArgument.class::isInstance)
      .map(CommandArgument.class::cast)
      .toList();
  }

  /// Splits up a list of command arguments based on the placement of optional argument types.
  ///
  /// @return a list of possible command argument paths, where each argument should be present
  static List<List<CommandArgument>> splitOptionals(List<CommandArgument> source) {
    final List<List<CommandArgument>> out = new ArrayList<>();

    for (int i = 0; i < source.size(); i++) {
      if (source.get(i) instanceof RequiredCommandArgument req && req.argumentType().optional()) {
        out.add(source.subList(0, i));
      }
    }

    out.add(source);
    return out;
  }
}
