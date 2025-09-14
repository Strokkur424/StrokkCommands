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

import net.strokkur.commands.internal.intermediate.suggestions.SuggestionProvider;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import java.util.Objects;

public class RequiredCommandArgumentImpl implements RequiredCommandArgument {

  private final BrigadierArgumentType argumentType;
  private final String name;
  private final Element element;
  private final @Nullable SuggestionProvider suggestionProvider;

  public RequiredCommandArgumentImpl(final BrigadierArgumentType argumentType, final String name, final Element element) {
    this.argumentType = argumentType;
    this.name = name;
    this.element = element;
    this.suggestionProvider = null;
  }

  public RequiredCommandArgumentImpl(final BrigadierArgumentType argumentType, final String name, final Element element, final SuggestionProvider suggestionProvider) {
    this.argumentType = argumentType;
    this.name = name;
    this.element = element;
    this.suggestionProvider = suggestionProvider;
  }

  @Override
  public BrigadierArgumentType getArgumentType() {
    return argumentType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Element element() {
    return element;
  }

  @Override
  @Nullable
  public SuggestionProvider getSuggestionProvider() {
    return suggestionProvider;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final RequiredCommandArgumentImpl that = (RequiredCommandArgumentImpl) o;
    return Objects.equals(argumentType, that.argumentType) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(argumentType, name);
  }
}
