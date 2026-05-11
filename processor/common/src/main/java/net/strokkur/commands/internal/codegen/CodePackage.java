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
package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class CodePackage implements CodeVisitable, Comparable<CodePackage> {
  private static final CodePackage JAVA_LANG = new CodePackage(new String[]{"java", "lang"});

  private final String[] paths;

  public static CodePackage of(String packageString) {
    return new CodePackage(packageString.split("\\."));
  }

  public CodePackage(String[] paths) {
    this.paths = paths;
  }

  public String path() {
    return String.join(".", paths);
  }

  @Override
  public <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitPackage(this);
  }

  public static boolean isRedundantImport(@Nullable CodePackage maybeRoot, CodePackage other) {
    return other.equals(JAVA_LANG) || Objects.equals(maybeRoot, other);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof final CodePackage that)) {
      return false;
    }
    return Arrays.deepEquals(paths, that.paths);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(paths);
  }

  @Override
  public int compareTo(CodePackage o) {
    return this.path().compareTo(o.path());
  }
}
