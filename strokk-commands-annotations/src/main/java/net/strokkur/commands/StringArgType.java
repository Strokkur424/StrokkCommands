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
package net.strokkur.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.strokkur.commands.annotations.arguments.StringArg;
import org.jetbrains.annotations.ApiStatus;

/// The type of a string argument. Can be declared with the [StringArg] annotation.
///
/// @see StringArg
public enum StringArgType {
  /// A word type.
  ///
  /// @see StringArg
  /// @see StringArgumentType#word()
  WORD("word"),
  /// A string type.
  ///
  /// @see StringArg
  /// @see StringArgumentType#string()
  STRING("string"),
  /// A greedy string type.
  ///
  /// @see StringArg
  /// @see StringArgumentType#greedyString()
  GREEDY("greedyString");

  private final String brigadierType;

  StringArgType(String brigadierType) {
    this.brigadierType = brigadierType;
  }

  /// The method name of the Brigadier string argument type definition
  /// for this specific type. Used internally in the command printer.
  @ApiStatus.Internal
  public String getBrigadierType() {
    return brigadierType;
  }
}
