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

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Attributable {

  @Nullable
  <T> T getAttribute(AttributeKey<T> key);

  <T> void setAttribute(AttributeKey<T> key, T value);

  void removeAttribute(AttributeKey<?> key);

  boolean hasAttribute(AttributeKey<?> key);

  default <V> void transferAttribute(AttributeKey<V> key, Attributable other) {
    ifAttributeExists(key, v -> other.setAttribute(key, v));
  }

  default <V> void ifAttributeExists(AttributeKey<V> key, Consumer<V> action) {
    final V value = getAttribute(key);
    if (value != null) {
      action.accept(value);
    }
  }

  default <U, V extends U> @Nullable U getEitherAttribute(AttributeKey<U> firstKey, AttributeKey<V> orElse) {
    if (hasAttribute(firstKey)) {
      return getAttributeNotNull(firstKey);
    }
    return getAttribute(orElse);
  }

  default <V> void editAttribute(AttributeKey<V> key, Function<V, V> action, @Nullable Supplier<V> ifNotExists) {
    if (hasAttribute(key)) {
      setAttribute(key, action.apply(getAttributeNotNull(key)));
    } else if (ifNotExists != null) {
      setAttribute(key, ifNotExists.get());
    }
  }

  default <V> void editAttributeMutable(AttributeKey<V> key, Consumer<V> action, @Nullable Supplier<V> ifNotExists) {
    if (hasAttribute(key)) {
      action.accept(getAttributeNotNull(key));
    } else if (ifNotExists != null) {
      setAttribute(key, ifNotExists.get());
    }
  }

  default <T> T getAttributeNotNull(AttributeKey<T> key) {
    return Objects.requireNonNull(getAttribute(key), "Attribute key " + key + " is null");
  }
}
