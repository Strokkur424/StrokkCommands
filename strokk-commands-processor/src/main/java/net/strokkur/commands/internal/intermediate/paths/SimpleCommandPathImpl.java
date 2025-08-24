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
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class SimpleCommandPathImpl<S extends CommandArgument> implements CommandPath<S> {

    protected final List<CommandPath<?>> children;
    protected @Nullable CommandPath<?> parent;
    protected List<S> arguments;

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
    public CommandPath<S> splitPath(final int index) {
        List<S> left = new ArrayList<>(arguments.subList(0, index));
        arguments = new ArrayList<>(arguments.subList(index, arguments.size()));
        SimpleCommandPathImpl<S> leftPath = createLeftSplit(left);

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
            builder.append(argument.getName()).append(" ");
        }

        for (final CommandPath<?> child : this.children) {
            builder.append("\n");
            builder.append(child.toString(indent + 1));
        }

        return builder.toString();
    }
}