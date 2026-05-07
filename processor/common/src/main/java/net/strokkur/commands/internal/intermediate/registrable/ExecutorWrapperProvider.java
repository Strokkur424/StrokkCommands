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
package net.strokkur.commands.internal.intermediate.registrable;

import net.strokkur.commands.internal.abstraction.SourceMethod;

/// Holds information about an executor wrapper.
///
/// @param wrapperMethod method implementing the wrapper
/// @param wrapperType the type of wrapper (Command return vs int return)
public record ExecutorWrapperProvider(
    SourceMethod wrapperMethod,
    WrapperType wrapperType
) {

  /// The method signature of the wrapper
  public enum WrapperType {
    /// `Command<S> wrap(Command<S>)`
    COMMAND,
    /// `Command<S> wrap(Command<S>, Method)`
    COMMAND_METHOD;

    public boolean withMethod() {
      return this == COMMAND_METHOD;
    }
  }
}
