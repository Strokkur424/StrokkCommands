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
package net.strokkur.commands.internal.intermediate.executable;

import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.exceptions.IllegalReturnTypeException;

import java.util.List;
import java.util.Objects;

public final class DefaultExecutableImpl extends ExecutableImpl implements DefaultExecutable {
  public DefaultExecutableImpl(SourceMethod executesMethod, List<ParameterType> parameters)
      throws IllegalReturnTypeException {
    super(executesMethod, parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(executesMethod().getEnclosed().getFullyQualifiedName(), executesMethod().getName(), parameterArguments());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DefaultExecutableImpl other) {
      return Objects.equals(executesMethod().getEnclosed().getFullyQualifiedName(), other.executesMethod().getEnclosed().getFullyQualifiedName())
          && Objects.equals(executesMethod().getName(), other.executesMethod().getName())
          && Objects.equals(parameterArguments(), other.parameterArguments());
    }
    return false;
  }
}
