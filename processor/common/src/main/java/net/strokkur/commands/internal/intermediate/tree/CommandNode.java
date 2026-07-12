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
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/// Represents a single node in a command tree.
public interface CommandNode extends Attributable {

  static CommandNode createArgument(CommandArgument arg) {
    return new ArgumentNode(arg);
  }

  static CommandNode createEmpty() {
    return new EmptyNode();
  }

  /// {@return the name of this node}
  String name();

  /// {@return the children of this node}
  @UnmodifiableView
  Collection<CommandNode> children();

  /// Executes the action for each node in the tree, starting with this node.
  default void forEach(Consumer<CommandNode> action) {
    action.accept(this);
    for (CommandNode child : this.children()) {
      child.forEach(action);
    }
  }

  /// Executes the action for each node in the tree, starting with the
  /// nodes deepest in the tree.
  default void forEachDepthFirst(Consumer<CommandNode> action) {
    for (CommandNode child : this.children()) {
      child.forEachDepthFirst(action);
    }
    action.accept(this);
  }

  /// Insert a node after this node.
  void addChild(CommandNode node);

  /// Insert an argument after this node.
  ///
  /// @return the node that represents this argument
  default CommandNode addArgument(CommandArgument argument) {
    final CommandNode node = new ArgumentNode(argument);
    addChild(node);
    return node;
  }

  /// Insert multiple arguments linearly after each other.
  ///
  /// @return the last node
  default CommandNode addArguments(List<? extends CommandArgument> arguments) {
    if (arguments.isEmpty()) {
      // the returned node is sometimes used for attributes which should be different
      // to the parent node, hence if no arguments are present to get a new node from,
      // insert an empty one.
      return addEmpty();
    }

    CommandNode node = this;
    for (CommandArgument argument : arguments) {
      node = node.addArgument(argument);
    }
    return node;
  }

  default CommandNode addEmpty() {
    final CommandNode node = new EmptyNode();
    addChild(node);
    return node;
  }
}
