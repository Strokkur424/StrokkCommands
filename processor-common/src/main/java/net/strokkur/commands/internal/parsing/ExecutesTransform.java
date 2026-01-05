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

import net.strokkur.commands.Executes;
import net.strokkur.commands.internal.NodeUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.exceptions.IllegalReturnTypeException;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.ExecutableImpl;
import net.strokkur.commands.internal.intermediate.executable.ParameterType;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;

import java.util.List;

public sealed class ExecutesTransform implements NodeTransform<SourceMethod>, ForwardingMessagerWrapper permits DefaultExecutesTransform {
  protected final CommandParser parser;
  protected final NodeUtils nodeUtils;

  public ExecutesTransform(CommandParser parser, NodeUtils nodeUtils) {
    this.parser = parser;
    this.nodeUtils = nodeUtils;
  }

  protected String transformName() {
    return "ExecutesTransform";
  }

  protected CommandNode createThisPath(final CommandNode parent, final SourceMethod element) throws MismatchedArgumentTypeException {
    final CommandNode out = createLiteralSequence(parent, element, Executes.class, Executes::value);
    return out == null ? parent : out;
  }

  protected void populatePath(
      final CommandNode node,
      final SourceMethod method,
      final List<ParameterType> args
  ) throws UnknownSenderException, IllegalReturnTypeException {
    final Executable executable = new ExecutableImpl(method, args);
    node.setAttribute(AttributeKey.EXECUTABLE, executable);
    nodeUtils().platformUtils().populateExecutesNode(executable, node, args);
  }

  @Override
  public final void transform(
      final CommandNode root,
      final SourceMethod element
  ) throws MismatchedArgumentTypeException, UnknownSenderException, IllegalReturnTypeException {
    debug("> {}: parsing {} for '{}'", transformName(), element, root.argument().argumentName());
    final CommandNode thisPath = this.createThisPath(root, element);

    final List<ParameterType> params = element.getParameters().stream()
        .map(nodeUtils()::parseParameter)
        .toList();
    final List<CommandArgument> args = params.stream()
        .filter(CommandArgument.class::isInstance)
        .map(CommandArgument.class::cast)
        .toList();

    final CommandNode out = thisPath.addChildren(args);

    populatePath(out, element, params);
    nodeUtils().applyRegistrableProvider(
        out,
        element,
        nodeUtils().requirementRegistry(),
        AttributeKey.REQUIREMENT_PROVIDER,
        "requirement"
    );
    nodeUtils().applyExecutorTransform(out, element);
    nodeUtils().platformUtils().populateNode(out, element);

    debug("  | {}: Current tree for thisPath: {}", transformName(), thisPath);
  }

  @Override
  public boolean requirement(final SourceMethod element) {
    return element.hasAnnotationInherited(Executes.class);
  }

  @Override
  public final NodeUtils nodeUtils() {
    return this.nodeUtils;
  }
}
