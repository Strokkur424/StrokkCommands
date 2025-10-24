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

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;

import java.util.List;

public class MethodTransform implements NodeTransform<SourceMethod> {
  private final List<NodeTransform<SourceMethod>> innerTransforms;
  private final PlatformUtils platformUtils;

  public MethodTransform(final PlatformUtils platformUtils, final ExecutesTransform executesTransform, final DefaultExecutesTransform defaultExecutesTransform) {
    this.innerTransforms = List.of(
        executesTransform,
        defaultExecutesTransform
    );
    this.platformUtils = platformUtils;
  }

  @Override
  public void transform(final CommandNode node, final SourceMethod element) throws MismatchedArgumentTypeException {
    for (final NodeTransform<SourceMethod> innerTransform : innerTransforms) {
      innerTransform.transformIfRequirement(node, element);
    }
  }

  @Override
  public boolean requirement(final SourceMethod element) {
    for (final NodeTransform<SourceMethod> innerTransform : innerTransforms) {
      if (innerTransform.requirement(element)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public PlatformUtils platformUtils() {
    return this.platformUtils;
  }
}
