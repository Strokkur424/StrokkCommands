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
package net.strokkur.commands.internal.paper.util;

import net.strokkur.jap.code.convert.ConvertToClassType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public enum ExecutorType {
  NONE,
  ENTITY(PaperClasses.ENTITY),
  PLAYER(PaperClasses.PLAYER);

  private final @Nullable ConvertToClassType classType;

  ExecutorType() {
    this.classType = null;
  }

  ExecutorType(ConvertToClassType classType) {
    this.classType = classType;
  }

  public ConvertToClassType classType() {
    return Objects.requireNonNull(classType);
  }

  /**
   * {@return if this executor is more restrictive or equal than another}
   */
  public boolean isMoreRestrictiveOrEqualThan(ExecutorType other) {
    return this.ordinal() >= other.ordinal();
  }
}
