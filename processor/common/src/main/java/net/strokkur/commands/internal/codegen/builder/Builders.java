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
package net.strokkur.commands.internal.codegen.builder;

import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeType;

import java.util.Arrays;

public class Builders {
  public static MethodBuilder method() {
    return new MethodBuilder();
  }

  public static MethodBuilder method(CodeClass declaringClass, String name) {
    return new MethodBuilder()
        .setDeclaringClass(declaringClass)
        .setName(name);
  }

  public static FieldBuilder field(String name, CodeType type) {
    return new FieldBuilder()
        .setName(name)
        .setType(type);
  }

  public static ClassBuilder classBuilder(String name, CodePackage codePackage) {
    return new ClassBuilder(name, codePackage);
  }

  public static ClassBuilder classBuilder(String fqn) {
    final String[] split = fqn.split("\\.");
    return new ClassBuilder(split[split.length - 1], new CodePackage(Arrays.copyOf(split, split.length - 1)));
  }

  private Builders() {
  }
}
