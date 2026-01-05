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

import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.internal.NodeUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.exceptions.IllegalReturnTypeException;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutableImpl;
import net.strokkur.commands.internal.intermediate.executable.ParameterType;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;

import java.util.List;

public final class DefaultExecutesTransform extends ExecutesTransform {

  public DefaultExecutesTransform(final CommandParser parser, final NodeUtils nodeUtils) {
    super(parser, nodeUtils);
  }

  @Override
  protected String transformName() {
    return "DefaultExecutesTransform";
  }

  @Override
  protected CommandNode createThisPath(final CommandNode parent, final SourceMethod element) throws MismatchedArgumentTypeException {
    final CommandNode out = createLiteralSequence(parent, element, DefaultExecutes.class, DefaultExecutes::value);
    return out == null ? parent : out;
  }

  @Override
  protected void populatePath(
      final CommandNode node,
      final SourceMethod method,
      final List<ParameterType> args
  ) throws UnknownSenderException, IllegalReturnTypeException {
    final DefaultExecutable executable = new DefaultExecutableImpl(method, args);
    node.setAttribute(AttributeKey.DEFAULT_EXECUTABLE, executable);
    nodeUtils().platformUtils().populateExecutesNode(executable, node, args);
  }

  @Override
  public boolean requirement(final SourceMethod element) {
    return element.hasAnnotationInherited(DefaultExecutes.class);
  }
}
