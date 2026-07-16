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

import net.strokkur.commands.internal.intermediate.attributes.AttributableHelper;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

abstract class AbstractCommandNode implements CommandNode, AttributableHelper {
  private static final String VALUE = "\033[0;36m";
  private static final String ATTRIBUTE = "\033[0;34m";
  private static final String NODE = "\033[1;32m";

  private static final String WHITE = "\033[0;37m";
  private static final String GRAY = "\033[0;30m";
  private static final String RESET = "\033[0m";

  protected final Map<String, Object> attributes;
  protected final List<CommandNode> children;

  AbstractCommandNode() {
    this.attributes = new TreeMap<>();
    this.children = new ArrayList<>();
  }

  @Override
  public Map<String, Object> attributeMap() {
    return attributes;
  }

  @Override
  public @UnmodifiableView List<CommandNode> children() {
    return Collections.unmodifiableList(children);
  }

  @Override
  public void addChild(CommandNode node) {
    this.children.add(node);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append(NODE).append(name()).append(RESET).append("\n");

    final List<String> attribList = attributes.entrySet().stream()
      .map(e -> ATTRIBUTE + e.getKey() + WHITE + ": " + VALUE + e.getValue())
      .toList();

    for (int i = 0; i < attribList.size() - 1; i++) {
      builder.append(GRAY).append("├ ").append(attribList.get(i)).append(RESET).append("\n");
    }

    if (children().isEmpty()) {
      if (!attribList.isEmpty()) {
        builder.append(GRAY).append("└ ").append(attribList.getLast()).append("\n");
      }
    } else {
      if (!attribList.isEmpty()) {
        builder.append(GRAY).append("┟ ").append(attribList.getLast()).append("\n");
      }
      for (int i = 0; i < children.size(); i++) {
        final String[] childToString = children.get(i).toString().split("\n");
        final boolean lastChild = i + 1 == children.size();

        if (childToString.length == 1 && lastChild) {
          builder.append(GRAY).append("┖ ").append(childToString[0]).append("\n");
          break;
        }

        builder.append(GRAY).append("┣ ").append(childToString[0]).append("\n");
        for (int ci = 1; ci < childToString.length - 1; ci++) {
          builder.append(GRAY).append("┃ ").append(childToString[ci]).append("\n");
        }

        if (lastChild) {
          builder.append(GRAY).append("┗ ");
        } else {
          builder.append(GRAY).append("┃ ");
        }

        builder.append(childToString[childToString.length - 1]);
        builder.append(RESET).append("\n");
      }
    }

    return builder.toString();
  }
}
