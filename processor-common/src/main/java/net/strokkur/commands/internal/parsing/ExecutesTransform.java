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

import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;

import java.util.ArrayList;
import java.util.List;

public abstract class ExecutesTransform implements NodeTransform<SourceMethod>, ForwardingMessagerWrapper {
  protected final CommandParser parser;
  protected final PlatformUtils platformUtils;

  protected ExecutesTransform(CommandParser parser, PlatformUtils platformUtils) {
    this.parser = parser;
    this.platformUtils = platformUtils;
  }

  protected String transformName() {
    return "ExecutesTransform";
  }

  protected CommandNode createThisPath(final CommandNode parent, final SourceMethod element) throws MismatchedArgumentTypeException {
    return this.createExecutesNode(parent, element);
  }

  protected int parametersToParse(final List<SourceParameter> parameters) {
    return parameters.size();
  }

  protected abstract void populatePath(
      final SourceMethod method,
      final CommandNode node,
      final List<CommandArgument> args,
      final List<SourceParameter> parameters
  );

  private void populatePathNoArguments(
      final SourceMethod method,
      final CommandNode node,
      final List<SourceParameter> parameters
  ) {
    populatePath(method, node, List.of(), parameters);
  }

  @Override
  public final void transform(final CommandNode root, final SourceMethod element) throws MismatchedArgumentTypeException {
    debug("> {}: parsing {} for '{}'", transformName(), element, root.argument().argumentName());
    final CommandNode thisPath = this.createThisPath(root, element);

    final List<SourceParameter> parameters = element.getParameters();
    final List<SourceParameter> arguments = new ArrayList<>(parameters.size() - 1);

    for (int i = 1, parametersSize = parametersToParse(parameters); i < parametersSize; i++) {
      arguments.add(parameters.get(i));
    }

    final List<CommandArgument> args = platformUtils().parseArguments(arguments, element.getEnclosed());
    if (args.isEmpty()) {
      populatePathNoArguments(element, thisPath, parameters);
      debug("  | {}: no arguments found. Current tree for thisPath: {}", transformName(), thisPath);
      return;
    }

    final CommandNode out = thisPath.addChildren(args);
    populatePath(element, out, args, parameters);
    debug("  | {}: found arguments! Current tree for thisPath: {}", transformName(), thisPath);
  }

  @Override
  public boolean requirement(final SourceMethod element) {
    return element.getAnnotationOptional(Executes.class).isPresent();
  }

  @Override
  public final PlatformUtils platformUtils() {
    return this.platformUtils;
  }
}
