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
package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceElement;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import org.jspecify.annotations.Nullable;

public interface CommandParser {

  /// @return null, if an exception occurred parsing the element
  @Nullable
  CommandNode createCommandTree(String name, SourceClass sourceClass);

  void parseElement(CommandNode root, SourceElement element) throws MismatchedArgumentTypeException;

  void parseClass(CommandNode root, SourceClass sourceClass) throws MismatchedArgumentTypeException;
}
