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

import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.internal.util.Utils;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToFieldMethodSource;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.util.Modifiers;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceField;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.stream.Collectors;

public record PrintedAccessPath(List<ExecuteAccess<?>> access) {

  public static PrintedAccessPath of(SequencedCollection<ExecuteAccess<?>> access) {
    if (access.isEmpty()) {
      throw new IllegalStateException("Access stack cannot be empty.");
    }
    return new PrintedAccessPath(List.copyOf(access));
  }

  public String name() {
    return Utils.getInstanceName(access);
  }

  public String elementName() {
    return access.getLast().name();
  }

  public PrintedAccessPath parent() {
    return PrintedAccessPath.of(access.subList(0, access.size() - 1));
  }

  public boolean hasParent() {
    return access.size() > 1;
  }

  public Set<PrintedAccessPath> allRequired() {
    final Set<PrintedAccessPath> out = new HashSet<>();
    if (!isInitializedField()) {
      out.add(this);
    }

    if (hasParent() && ((isClass() && !isStaticClass()) || isInitializedField())) {
      out.addAll(parent().allRequired());
    }

    return out;
  }

  public ConvertToExpression getInitializer(CommandInformation c) {
    if (!hasParent() && c.constructor() != null) {
      return c.sourceClass().ctor(c.constructor().parameters().stream()
        .map(p -> Expressions.variable(p.name()))
        .toArray(ConvertToExpression[]::new)
      );
    }

    if (access.getLast() instanceof InstanceAccess(SourceClassLike classLike)) {
      if (classLike.isStatic()) {
        return classLike.ctor();
      } else {
        return classLike.ctor().setSource(parent().getAccess());
      }
    }
    final FieldAccess fa = (FieldAccess) access.getLast();
    return fa.toClassType().ctor();
  }

  public ConvertToFieldMethodSource getAccess() {
    if (!isInitializedField()) {
      return Expressions.variable(name());
    }
    return parent().getAccess().chainField(elementName());
  }

  public CodeClassType type() {
    return access.getLast().toClassType();
  }

  // Util
  private boolean isInitializedField() {
    return access.getLast() instanceof FieldAccess(SourceField fieldElement) &&
      (fieldElement.initializer() != null || fieldElement.modifiers().contains(Modifiers.FINAL));
  }

  public boolean isClass() {
    return access.getLast() instanceof InstanceAccess(
      SourceClassLike classElement
    ) && classElement instanceof SourceClass;
  }

  public boolean isStaticClass() {
    return access.getLast() instanceof InstanceAccess(SourceClassLike classElement) && classElement.isStatic();
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

  @Override
  public String toString() {
    return access.stream()
      .map(Object::toString)
      .collect(Collectors.joining(", ", "[", "]"));
  }
}
