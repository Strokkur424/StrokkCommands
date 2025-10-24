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
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.attributes.AttributableHelper;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class CommandNodeImpl implements CommandNode, AttributableHelper {
  private final Map<String, Object> attributes;
  private final Set<CommandNode> children;
  private final @Nullable CommandNode parent;
  private final CommandArgument argument;

  public CommandNodeImpl(final @Nullable CommandNode parent, final CommandArgument argument) {
    this.attributes = new TreeMap<>();
    this.children = new HashSet<>();
    this.parent = parent;
    this.argument = argument;
  }

  @Override
  public Map<String, Object> attributeMap() {
    return this.attributes;
  }

  @Override
  public CommandArgument argument() {
    return this.argument;
  }

  @Override
  @Nullable
  public CommandNode parent() {
    return this.parent;
  }

  @Override
  @UnmodifiableView
  public Collection<CommandNode> children() {
    return Collections.unmodifiableSet(this.children);
  }

  @Override
  public CommandNode addChild(final CommandArgument argument) throws MismatchedArgumentTypeException {
    for (final CommandNode child : this.children) {
      if (child.argument() instanceof MultiLiteralCommandArgument childMulti) {
        if (argument instanceof MultiLiteralCommandArgument argMulti) {
          if (childMulti.literals().equals(argMulti.literals())) {
            // The same multi literals
            return child;
          }
          for (final String literal : childMulti.literals()) {
            if (argMulti.literals().contains(literal)) {
              throw new MismatchedArgumentTypeException("The multi literal " + childMulti.literals() + " contains duplicate literals from the existing multiliteral " + argMulti.literals());
            }
          }
          continue;
        }

        if (childMulti.literals().contains(argument.argumentName())) {
          throw new MismatchedArgumentTypeException("The multi literal " + childMulti.literals() + " already contains the name " + argument.argumentName() + ".");
        }
        continue;
      }

      if (!child.argument().argumentName().equals(argument.argumentName())) {
        continue;
      }

      if (child.argument() instanceof LiteralCommandArgument && argument instanceof LiteralCommandArgument) {
        // since the name is equal, these are the same literal, meaning we can just give back this existing child
        return child;
      }

      if (child.argument() instanceof RequiredCommandArgument childReq && argument instanceof RequiredCommandArgument argReq) {
        if (childReq.argumentType().equals(argReq.argumentType())) {
          // great, they have the same name and type!
          return child;
        }
        throw new MismatchedArgumentTypeException("An argument with the name of '%s' already exists, but the argument type was different. Expected: %s. Found: %s".formatted(
            child.argument().argumentName(),
            childReq.argumentType().initializer(),
            argReq.argumentType().initializer()
        ));
      }

      throw new MismatchedArgumentTypeException("An argument with the name of '%s' already exists, but one was a literal and one was an argument.".formatted(
          child.argument().argumentName()
      ));
    }

    final CommandNodeImpl newNode = new CommandNodeImpl(this, argument);
    this.children.add(newNode);
    return newNode;
  }

  public String toString() {
    return this.toString(0);
  }

  @Override
  public String toString(final int indent) {
    final StringBuilder builder = new StringBuilder();
    builder.append("| ".repeat(indent));

    switch (this.argument) {
      case MultiLiteralCommandArgument multi -> builder.append('[').append(String.join("|", multi.literals())).append(']');
      case LiteralCommandArgument lit -> builder.append(lit.literal());
      case RequiredCommandArgument req -> builder.append(req.argumentName()).append(" (").append(req.argumentType().initializer()).append(')');
      default -> throw new IllegalStateException("Unknown argument type class: " + this.argument.getClass());
    }

    if (!this.attributes.isEmpty()) {
      builder.append(" ".repeat(Math.max(2, 80 - builder.length())));
      this.attributes.forEach((k, v) -> builder.append(k).append('=').append(v).append(' '));
    }

    for (final CommandNode child : this.children) {
      builder.append('\n').append(child.toString(indent + 1));
    }

    return builder.toString();
  }
}
