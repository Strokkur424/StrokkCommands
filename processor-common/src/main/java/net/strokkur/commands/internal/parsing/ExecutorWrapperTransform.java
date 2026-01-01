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
package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.ExecutorWrapper;
import net.strokkur.commands.internal.NodeUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.ExecutorWrapperProvider;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;

public final class ExecutorWrapperTransform implements NodeTransform<SourceMethod>, ForwardingMessagerWrapper {
  private final NodeUtils nodeUtils;

  public ExecutorWrapperTransform(final NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  @Override
  public void transform(final CommandNode node, final SourceMethod element) {
    debug("> ExecutorWrapperTransform: parsing {} for '{}'", element, node.argument().argumentName());
    final ExecutorWrapperProvider provider = new ExecutorWrapperProvider(element);
    node.setAttribute(AttributeKey.EXECUTOR_WRAPPER, provider);
  }

  @Override
  public boolean requirement(final SourceMethod element) {
    return element.hasAnnotation(ExecutorWrapper.class);
  }

  @Override
  public NodeUtils nodeUtils() {
    return this.nodeUtils;
  }
}
