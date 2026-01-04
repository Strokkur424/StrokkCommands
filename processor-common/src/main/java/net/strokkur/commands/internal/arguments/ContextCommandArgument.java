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

import net.strokkur.commands.internal.abstraction.SourceElement;

/// Represents a parameter annotated with {@code @Context} that should be injected
/// with the Brigadier {@code CommandContext} at runtime.
///
/// Unlike other argument types, context arguments do not represent actual command
/// arguments from the user input. Instead, they are injected directly from the
/// execution context.
public record ContextCommandArgument(SourceElement element) implements CommandArgument {

  @Override
  public String argumentName() {
    return "ctx";
  }
}
