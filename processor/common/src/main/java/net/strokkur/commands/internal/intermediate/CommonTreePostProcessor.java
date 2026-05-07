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

import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.MessagerWrapper;

public non-sealed abstract class CommonTreePostProcessor implements TreePostProcessor {
  private final MessagerWrapper delegateMessager;

  public CommonTreePostProcessor(MessagerWrapper delegateMessager) {
    this.delegateMessager = delegateMessager;
  }

  public final void applyDefaultExecutorPaths(final CommandNode node) {
    final DefaultExecutable defaultExecutable = node.getAttribute(AttributeKey.DEFAULT_EXECUTABLE);

    if (defaultExecutable == null) {
      node.children().forEach(this::applyDefaultExecutorPaths);
      return;
    }

    node.children().forEach(
        child -> applyDefaultExecutorPathIfUnset(child, defaultExecutable)
    );
  }

  private void applyDefaultExecutorPathIfUnset(final CommandNode node, final DefaultExecutable def) {
    final DefaultExecutable defaultExecutable = node.getAttributeOrSet(AttributeKey.DEFAULT_EXECUTABLE, def);
    for (final CommandNode child : node.children()) {
      applyDefaultExecutorPathIfUnset(child, defaultExecutable);
    }
  }

  @Override
  public final MessagerWrapper delegateMessager() {
    return delegateMessager;
  }
}
