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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

abstract class SimpleCommandPathImpl<S extends CommandArgument> implements CommandPath<S> {

    protected final List<CommandPath<?>> children;
    protected @Nullable CommandPath<?> parent;
    protected List<S> arguments;
    protected Map<String, Object> attributes = new TreeMap<>();

    public SimpleCommandPathImpl(final List<S> arguments) {
        this.children = new ArrayList<>();
        this.parent = null;
        this.arguments = arguments;
    }

    abstract SimpleCommandPathImpl<S> createLeftSplit(final List<S> args);

    @Override
    public @UnmodifiableView List<S> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public @Nullable CommandPath<?> getParent() {
        return this.parent;
    }

    @Override
    public void setParent(final @Nullable CommandPath<?> parent) {
        this.parent = parent;
    }

    @Override
    public @UnmodifiableView List<CommandPath<?>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void removeChild(final CommandPath<?> child) {
        child.setParent(null);
        this.children.remove(child);
    }

    @Override
    public void addChild(final CommandPath<?> child) {
        if (child.getParent() != null) {
            // Just a bit of cleanup to avoid dangling references
            child.getParent().removeChild(child);
        }

        this.children.add(child);
        child.setParent(this);
    }

    @Override
    @Nullable
    public <T> T getAttribute(final AttributeKey<T> key) {
        if (attributes.containsKey(key.key())) {
            return (T) attributes.get(key.key());
        } else {
            return key.defaultValue();
        }
    }

    @Override
    public <T> void setAttribute(final AttributeKey<T> key, final T value) {
        attributes.put(key.key(), value);
    }

    @Override
    public void removeAttribute(final AttributeKey<?> key) {
        attributes.remove(key.key());
    }

    @Override
    public boolean hasAttribute(final AttributeKey<?> key) {
        return attributes.containsKey(key.key());
    }

    @Override
    public CommandPath<S> splitPath(final int index) {
        List<S> left = new ArrayList<>(arguments.subList(0, index));
        arguments = new ArrayList<>(arguments.subList(index, arguments.size()));

        final SimpleCommandPathImpl<S> leftPath = createLeftSplit(left);
        leftPath.attributes = new HashMap<>(attributes);

        if (this.parent != null) {
            this.parent.removeChild(this);
            this.parent.addChild(leftPath);
        }

        leftPath.children.clear();
        leftPath.addChild(this);

        return leftPath;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public String toString(int indent) {
        final StringBuilder builder = new StringBuilder();
        builder.append("| ".repeat(indent));

        if (this.arguments.isEmpty() && this.children.isEmpty()) {
            return builder.append("<empty>").toString();
        }

        for (final S argument : this.arguments) {
            builder.append(argument.getName());
        }

        // Add attributes
        if (!attributes.isEmpty()) {
            builder.append(" ".repeat(40 - builder.length()));
            builder.append("Attributes: {");
            this.attributes.forEach((key, value) -> builder.append(key).append(" = ").append(value).append(" "));
            builder.deleteCharAt(builder.length() - 1);
            builder.append("}");
        }

        for (final CommandPath<?> child : this.children) {
            builder.append("\n");
            builder.append(child.toString(indent + 1));
        }

        return builder.toString();
    }
}