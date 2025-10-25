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
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.attributes.ExecutableImpl;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;

import java.util.ArrayList;
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
    return this.createExecutesNode(parent, element);
  }

  protected void populatePath(final CommandNode node, final SourceMethod method, final List<CommandArgument> args, final List<SourceParameter> parameters)
      throws UnknownSenderException {
    final Executable executable = new ExecutableImpl(method, args);
    node.setAttribute(AttributeKey.EXECUTABLE, executable);
    nodeUtils().platformUtils().populateExecutesNode(executable, node, parameters);
  }

  protected int parametersToParse(final List<SourceParameter> parameters) {
    return parameters.size();
  }

  @Override
  public final void transform(final CommandNode root, final SourceMethod element) throws MismatchedArgumentTypeException, UnknownSenderException {
    debug("> {}: parsing {} for '{}'", transformName(), element, root.argument().argumentName());
    final CommandNode thisPath = this.createThisPath(root, element);

    final List<SourceParameter> parameters = element.getParameters();
    final List<SourceParameter> arguments = new ArrayList<>(parameters.size() - 1);

    for (int i = nodeUtils().platformUtils().executableFirstIndexToParse(parameters), parametersSize = parametersToParse(parameters); i < parametersSize; i++) {
      arguments.add(parameters.get(i));
    }

    final List<CommandArgument> args = nodeUtils().parseArguments(arguments);
    final CommandNode out = thisPath.addChildren(args);

    this.nodeUtils().applyRegistrableProvider(
        out,
        element,
        nodeUtils().requirementRegistry(),
        AttributeKey.REQUIREMENT_PROVIDER,
        "requirement"
    );

    populatePath(out, element, args, parameters);
    debug("  | {}: Current tree for thisPath: {}", transformName(), thisPath);
  }

  @Override
  public boolean requirement(final SourceMethod element) {
    return element.getAnnotationOptional(Executes.class).isPresent();
  }

  @Override
  public final NodeUtils nodeUtils() {
    return this.nodeUtils;
  }
}
