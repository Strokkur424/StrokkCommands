package net.strokkur.commands.internal.intermediate.tree;

import net.strokkur.commands.internal.intermediate.attributes.AttributableHelper;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

abstract class AbstractCommandNode implements CommandNode, AttributableHelper {
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
  public CommandNode addChild(CommandNode node) {
    this.children.add(node);
    return node;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(name());
    List<String> attribList = attributes.entrySet().stream()
      .map(e -> e.getKey() + ": " + e.getValue())
      .toList();

    for (int i = 0; i < attribList.size() - 1; i++) {
      builder.append("├ ").append(attribList.get(i));
    }

    if (children().isEmpty()) {
      builder.append("└ ").append(attribList.getLast());
    } else {
      builder.append("┟ ").append(attribList.getLast());
      for (int i = 0; i < children.size(); i++) {
        String[] childToString = children.get(i).toString().split("\n");
        boolean lastChild = i + 1 == children.size();

        if (childToString.length == 1 && lastChild) {
          builder.append("┖ ").append(childToString[0]);
          break;
        }

        builder.append("┣ ").append(childToString[0]);
        for (int ci = 1; ci < childToString.length - 1; ci++) {
          builder.append("┃ ").append(childToString[ci]);
        }

        if (lastChild) {
          builder.append("┗ ");
        } else {
          builder.append("┃ ");
        }

        builder.append(childToString[childToString.length - 1]);
      }
    }

    return builder.toString();
  }
}
