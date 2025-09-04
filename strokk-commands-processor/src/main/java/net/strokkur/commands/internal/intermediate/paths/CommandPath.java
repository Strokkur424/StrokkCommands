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
package net.strokkur.commands.internal.intermediate.paths;

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public interface CommandPath<S extends CommandArgument> {

    @UnmodifiableView
    List<S> getArguments();

    @Nullable
    CommandPath<?> getParent();

    void setParent(@Nullable CommandPath<?> parent);

    @UnmodifiableView
    List<CommandPath<?>> getChildren();

    void removeChild(CommandPath<?> child);

    void addChild(CommandPath<?> child);

    @Nullable
    <T> T getAttribute(AttributeKey<T> key);

    <T> void setAttribute(AttributeKey<T> key, T value);

    void removeAttribute(AttributeKey<?> key);

    boolean hasAttribute(AttributeKey<?> key);

    /**
     * Splits the argument path of this path and returns an instance of the first half of the split
     * path. The second split path will be the same instance as this path.
     *
     * @param index the index to split at
     * @return the new, left side instance of the split
     */
    CommandPath<S> splitPath(int index);

    /**
     * Debug method.
     */
    String toString(int indent);

    String toStringNoChildren();

    default <T> T getAttributeNotNull(AttributeKey<T> key) {
        return Objects.requireNonNull(getAttribute(key), "Attribute key " + key + " is null");
    }

    default void forEachChild(Consumer<CommandPath<?>> action) {
        this.getChildren().forEach(child -> child.forEachChildAccept(action));
    }

    default void forEachChildAccept(Consumer<CommandPath<?>> action) {
        this.getChildren().forEach(child -> child.forEachChildAccept(action));
        action.accept(this);
    }

    /**
     * Returns the number of same arguments at the start of this and the provided path.
     *
     * @param other other path
     * @return number of arguments that are the same at the start
     */
    default int getSameArguments(CommandPath<?> other) {
        final List<?> arguments = getArguments();
        final List<?> otherArguments = other.getArguments();
        final int min = Math.min(arguments.size(), otherArguments.size());

        for (int i = 0; i < min; i++) {
            if (!arguments.get(i).equals(otherArguments.get(i))) {
                return i;
            }
        }

        return min;
    }
}
