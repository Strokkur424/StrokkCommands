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
package net.strokkur.commands.internal.intermediate.tree;

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.util.IOExceptionIgnoringConsumer;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/// Represents a single node in a command tree.
public interface CommandNode extends Attributable {

  static CommandNode createRoot(final LiteralCommandArgument name) {
    return new CommandNodeImpl(null, name);
  }

  /// {@return the argument held by this node}
  CommandArgument argument();

  /// {@return the parent of this node. May be `null` if this node is a root node}
  @Nullable
  CommandNode parent();

  /// {@return the children of this node}
  @UnmodifiableView
  Collection<CommandNode> children();

  /// Executes the action for each node in the tree, starting with this node.
  default void forEach(final Consumer<CommandNode> action) {
    action.accept(this);
    for (final CommandNode child : this.children()) {
      child.forEach(action);
    }
  }

  /// Executes the action for each node in the tree, starting with the
  /// nodes deepest in the tree.
  default void forEachDepthFirst(final Consumer<CommandNode> action) {
    for (final CommandNode child : this.children()) {
      child.forEachDepthFirst(action);
    }
    action.accept(this);
  }

  /// Executes the action for each node in the tree, starting with this node.
  default void forEachIo(final IOExceptionIgnoringConsumer<CommandNode> action) throws IOException {
    action.accept(this);
    for (final CommandNode child : this.children()) {
      child.forEachIo(action);
    }
  }

  /// Executes the action for each node in the tree, starting with the
  /// nodes deepest in the tree.
  default void forEachDepthFirstIo(final IOExceptionIgnoringConsumer<CommandNode> action) throws IOException {
    for (final CommandNode child : this.children()) {
      child.forEachIo(action);
    }
    action.accept(this);
  }

  /// Insert an argument after this node.
  ///
  /// @return the node which represents this argument
  CommandNode addChild(CommandArgument argument) throws MismatchedArgumentTypeException;

  /// Insert multiple arguments linearly after each other.
  ///
  /// @return the last node
  default CommandNode addChildren(final List<CommandArgument> arguments) throws MismatchedArgumentTypeException {
    CommandNode node = this;
    for (final CommandArgument argument : arguments) {
      node = node.addChild(argument);
    }
    return node;
  }

  String toString(int indent);
}
