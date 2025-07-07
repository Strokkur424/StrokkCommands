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

import net.strokkur.commands.internal.intermediate.SuggestionProvider;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import java.util.Objects;

public final class RequiredArgumentInformation implements ArgumentInformation {
    private final String argumentName;
    private final VariableElement element;
    private final BrigadierArgumentType type;

    private @Nullable SuggestionProvider suggestionProvider = null;

    public RequiredArgumentInformation(String argumentName, VariableElement element, BrigadierArgumentType type) {
        this.argumentName = argumentName;
        this.element = element;
        this.type = type;
    }

    @Override
    public String toString() {
        return "RequiredArgumentInformation{" +
               "argumentName='" + argumentName + '\'' +
               ", type=" + type +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequiredArgumentInformation that = (RequiredArgumentInformation) o;
        return Objects.equals(getArgumentName(), that.getArgumentName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArgumentName(), getType());
    }

    @Override
    public String getArgumentName() {
        return argumentName;
    }

    @Override
    public VariableElement getElement() {
        return element;
    }

    public BrigadierArgumentType getType() {
        return type;
    }

    @Override
    @Nullable
    public SuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
    }

    @Override
    public void setSuggestionProvider(@Nullable SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }
}
