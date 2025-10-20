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

import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public class MethodTransform implements NodeTransform<ExecutableElement> {
  private final List<NodeTransform<ExecutableElement>> innerTransforms;
  private final MessagerWrapper messagerWrapper;
  private final BrigadierArgumentConverter converter;

  public MethodTransform(final CommandParser parser, final MessagerWrapper messager, final BrigadierArgumentConverter converter) {
    this.innerTransforms = List.of(
        new ExecutesTransform(parser, messager, converter),
        new DefaultExecutesTransform(parser, messager, converter)
    );
    this.messagerWrapper = messager;
    this.converter = converter;
  }

  @Override
  public void transform(final CommandNode node, final ExecutableElement element) throws MismatchedArgumentTypeException {
    for (final NodeTransform<ExecutableElement> innerTransform : innerTransforms) {
      innerTransform.transformIfRequirement(node, element);
    }
  }

  @Override
  public boolean requirement(final ExecutableElement element) {
    for (final NodeTransform<ExecutableElement> innerTransform : innerTransforms) {
      if (innerTransform.requirement(element)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public BrigadierArgumentConverter argumentConverter() {
    return this.converter;
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return this.messagerWrapper;
  }
}
