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

import net.strokkur.commands.internal.util.Classes;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public interface DefaultExecutable extends Executable {
  Type defaultExecutableArgumentTypes();

  enum Type {
    NONE(null, Set.of()),
    ARRAY("ctx.getInput().split(\" \")", Set.of()),
    LIST("Collections.unmodifiableList(Arrays.asList(ctx.getInput().split(\" \")))", Set.of(Classes.COLLECTIONS, Classes.ARRAYS));

    private final @Nullable String getter;
    private final Set<String> imports;

    Type(@Nullable final String getter, final Set<String> imports) {
      this.getter = getter;
      this.imports = imports;
    }

    public @Nullable String getGetter() {
      return this.getter;
    }

    public Set<String> getImports() {
      return this.imports;
    }
  }
}
