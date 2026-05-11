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
import net.strokkur.commands.internal.util.ConvertableTo;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public record CodeField(
    String name,
    CodeType type,
    @Nullable CodeExpression initialiser,
    Set<Modifiers> modifiers,
    List<CodeAnnotation> annotations
) implements CodeVisitable, ConvertableTo.Self<CodeField> {

  @Override
  public <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitField(this);
  }
}
