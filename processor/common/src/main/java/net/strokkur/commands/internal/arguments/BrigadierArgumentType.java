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

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public record BrigadierArgumentType(String initializer, String retriever, Set<String> imports) {

  public static BrigadierArgumentType of(String initializer, String retriever) {
    return new BrigadierArgumentType(initializer, retriever, Set.of());
  }

  public static BrigadierArgumentType of(String initializer, String retriever, String singleImport) {
    return new BrigadierArgumentType(initializer, retriever, Set.of(singleImport));
  }

  public static BrigadierArgumentType of(String initializer, String retriever, Set<String> imports) {
    return new BrigadierArgumentType(initializer, retriever, imports);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof BrigadierArgumentType(String initializer1, String retriever1, Set<String> imports1))) {
      return false;
    }

    return Objects.equals(retriever(), retriever1)
        && Objects.equals(initializer(), initializer1)
        && Objects.equals(imports(), imports1);
  }

  @Override
  public int hashCode() {
    return Objects.hash(initializer(), retriever(), imports());
  }
}
