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
package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.util.Utils;

import java.util.List;
import java.util.Objects;

public record PrintedAccessPath(List<ExecuteAccess<?>> access) {
  public PrintedAccessPath(List<ExecuteAccess<?>> access) {
    if (access.isEmpty()) {
      throw new IllegalStateException("Access stack cannot be empty.");
    }
    this.access = List.copyOf(access);
  }

  public boolean needsCreating() {
    return !(access.getLast() instanceof FieldAccess field) || !field.getElement().isInitialized();
  }

  public String name() {
    return Utils.getInstanceName(access);
  }

  public String elementName() {
    return access.getLast().getElement().getName();
  }

  public PrintedAccessPath parent() {
    return new PrintedAccessPath(access.subList(0, access.size() - 1));
  }

  public PrintedAccessPath requiredParent() {
    if (needsCreating()) {
      return this;
    }
    return parent().requiredParent();
  }

  public AsExpression getVariableAccess() {
    if (needsCreating()) {
      // The field with this name
      return CodeExpression.variable(name());
    }

    return Builders.fieldAccess(elementName()).setSource(parent().getVariableAccess());
  }

  public CodeType type() {
    return access.getLast().getAsCodeType();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof final PrintedAccessPath that)) {
      return false;
    }
    return Objects.equals(name(), that.name());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name());
  }
}
