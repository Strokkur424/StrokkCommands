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

import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public class MethodTransform implements PathTransform<ExecutableElement> {
  private final List<PathTransform<ExecutableElement>> innerTransforms;

  public MethodTransform(final CommandParser parser, final MessagerWrapper messager) {
    this.innerTransforms = List.of(
        new ExecutesTransform(parser, messager),
        new DefaultExecutesTransform(parser, messager)
    );
  }

  @Override
  public void transform(final CommandPath<?> parent, final ExecutableElement element) {
    for (final PathTransform<ExecutableElement> innerTransform : innerTransforms) {
      innerTransform.transformIfRequirement(parent, element);
    }
  }

  @Override
  public boolean requirement(final ExecutableElement element) {
    for (final PathTransform<ExecutableElement> innerTransform : innerTransforms) {
      if (innerTransform.requirement(element)) {
        return true;
      }
    }
    return false;
  }
}
