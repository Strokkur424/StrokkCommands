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
package net.strokkur.commands.internal.intermediate.registrable;

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceField;

import java.util.List;

record FieldImpl(SourceClass sourceClass, SourceField field) implements SuggestionProvider, RequirementProvider {
  @Override
  public String getSuggestionString() {
    return "%s.%s".formatted(sourceClass.getSourceName(), field.getName());
  }

  @Override
  public String getRequirementString() {
    return "%s.%s.test(source)".formatted(sourceClass.getSourceName(), field.getName());
  }

  @Override
  public SourceClass getSourceClass() {
    return this.sourceClass;
  }

  @Override
  public List<SourceClass> getSourceClasses() {
    return List.of(this.sourceClass);
  }
}
