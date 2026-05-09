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

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeField;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.Modifiers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FieldBuilder {
  private @Nullable String name = null;
  private @Nullable CodeType type = null;
  private @Nullable CodeExpression initialiser;
  private final Set<Modifiers> modifiers = new HashSet<>();
  private final List<CodeAnnotation> annotations = new ArrayList<>();

  FieldBuilder() {
    // package-private ctor
  }

  public FieldBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public FieldBuilder setType(CodeType type) {
    this.type = type;
    return this;
  }

  public FieldBuilder setInitialiser(@Nullable CodeExpression initialiser) {
    this.initialiser = initialiser;
    return this;
  }

  public FieldBuilder setModifiers(Set<Modifiers> modifiers) {
    this.modifiers.clear();
    this.modifiers.addAll(modifiers);
    return this;
  }

  public FieldBuilder addAnnotation(CodeAnnotation annotation) {
    this.annotations.add(annotation);
    return this;
  }

  public CodeField build() {
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
    return new CodeField(
        name, type, initialiser, Set.copyOf(modifiers), List.copyOf(annotations)
    );
  }
}
