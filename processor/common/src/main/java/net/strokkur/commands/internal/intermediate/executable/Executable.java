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
import net.strokkur.commands.internal.abstraction.SourcePrimitive;
import net.strokkur.commands.internal.abstraction.SourceType;
import net.strokkur.commands.internal.abstraction.VoidSourceType;
import net.strokkur.commands.internal.exceptions.IllegalReturnTypeException;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;

public interface Executable extends Parameterizable, Attributable {
  SourceMethod executesMethod();

  ReturnType returnType();

  enum ReturnType {
    INT,
    VOID;

    public static ReturnType getType(final SourceType type) throws IllegalReturnTypeException {
      return switch (type) {
        case VoidSourceType ignored -> ReturnType.VOID;
        case SourcePrimitive primitive -> {
          if (primitive.getName().equals("int")) {
            yield ReturnType.INT;
          }
          throw new IllegalReturnTypeException(type);
        }
        default -> throw new IllegalReturnTypeException(type);
      };
    }
  }
}
