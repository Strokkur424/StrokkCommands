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
package net.strokkur.commands.internal.intermediate;

import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.tree.ArgumentNode;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.jap.source.util.MessagerWrapper;

import java.util.Optional;
import java.util.ServiceLoader;

public abstract class TreePostProcessor implements ForwardingMessagerWrapper {
  private final MessagerWrapper delegateMessager = StrokkCommandsProcessor.messagerWrapper();

  public static TreePostProcessor get() {
    class Holder {
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
      static final Optional<TreePostProcessor> INSTANCE = ServiceLoader.load(
        TreePostProcessor.class, TreePostProcessor.class.getClassLoader()
      ).findFirst();
    }

    return Holder.INSTANCE.orElseThrow(() -> new RuntimeException("No instance of TreePostProcessor found."));
  }

  public abstract void cleanupPath(CommandNode root);

  public final void applyDefaultExecutorPaths(CommandNode node) {
    final DefaultExecutable defaultExecutable = node.getAttribute(AttributeKey.DEFAULT_EXECUTABLE);

    if (defaultExecutable == null) {
      node.children().forEach(this::applyDefaultExecutorPaths);
      return;
    }

    node.children().forEach(
      child -> applyDefaultExecutorPathIfUnset(child, defaultExecutable)
    );
  }

  private void applyDefaultExecutorPathIfUnset(CommandNode node, DefaultExecutable def) {
    final DefaultExecutable defaultExecutable;

    if (node instanceof ArgumentNode) {
      // Only set explicitly on argument nodes.
      defaultExecutable = node.getAttributeOrSet(AttributeKey.DEFAULT_EXECUTABLE, def);
    } else {
      defaultExecutable = node.getAttributeOr(AttributeKey.DEFAULT_EXECUTABLE, def);
    }

    for (CommandNode child : node.children()) {
      applyDefaultExecutorPathIfUnset(child, defaultExecutable);
    }
  }

  @Override
  public final MessagerWrapper delegateMessager() {
    return delegateMessager;
  }
}
