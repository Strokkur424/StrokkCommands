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
package net.strokkur.commands.internal.intermediate.attributes;

import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.arguments.CommandArgument;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExecutableImpl implements Executable, AttributableHelper {
  private final SourceMethod executesMethod;
  private final List<CommandArgument> parameterArguments;
  private final Map<String, Object> attributeMap = new TreeMap<>();

  public ExecutableImpl(SourceMethod executesMethod, List<CommandArgument> parameterArguments) {
    this.executesMethod = executesMethod;
    this.parameterArguments = parameterArguments;
  }

  @Override
  public Map<String, Object> attributeMap() {
    return this.attributeMap;
  }

  @Override
  public SourceMethod executesMethod() {
    return executesMethod;
  }

  @Override
  public List<CommandArgument> parameterArguments() {
    return parameterArguments;
  }

  @Override
  public String toString() {
    return "ExecutableImpl[" + executesMethod + ']';
  }
}
