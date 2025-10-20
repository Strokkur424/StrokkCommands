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
import net.strokkur.commands.annotations.Executor;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.ExecutableImpl;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

sealed class ExecutesTransform implements NodeTransform<ExecutableElement>, ForwardingMessagerWrapper permits DefaultExecutesTransform {
  protected final CommandParser parser;
  private final MessagerWrapper delegateMessager;
  private final BrigadierArgumentConverter converter;

  protected ExecutesTransform(CommandParser parser, MessagerWrapper delegateMessager, BrigadierArgumentConverter converter) {
    this.parser = parser;
    this.delegateMessager = delegateMessager;
    this.converter = converter;
  }

  protected String transformName() {
    return "ExecutesTransform";
  }

  protected CommandNode createThisPath(final CommandNode parent, final ExecutableElement element) {
    return this.createExecutesNode(parent, element);
  }

  protected int parametersToParse(final List<? extends VariableElement> parameters) {
    return parameters.size();
  }

  protected void populatePath(
      final ExecutableElement method,
      final CommandNode node,
      final ExecutorType type,
      final List<CommandArgument> args,
      final List<? extends VariableElement> parameters) {
    node.setAttribute(AttributeKey.EXECUTABLE, new ExecutableImpl(type, method, args));
  }

  protected void populatePathNoArguments(
      final ExecutableElement method,
      final CommandNode node,
      final ExecutorType type,
      final List<? extends VariableElement> parameters) {
    node.setAttribute(AttributeKey.EXECUTABLE, new ExecutableImpl(type, method, List.of()));
  }

  @Override
  public final void transform(final CommandNode root, final ExecutableElement element) throws MismatchedArgumentTypeException {
//    debug("> {}: parsing {} for '{}'", transformName(), element, root.toStringNoChildren());
    final CommandNode thisPath = this.createThisPath(root, element);

    ExecutorType type = ExecutorType.NONE;

    final List<? extends VariableElement> parameters = element.getParameters();
    final List<VariableElement> arguments = new ArrayList<>(parameters.size() - 1);

    for (int i = 1, parametersSize = parametersToParse(parameters); i < parametersSize; i++) {
      final VariableElement param = parameters.get(i);

      if (i == 1 && param.getAnnotation(Executor.class) != null) {
        if (param.asType().toString().equals(Classes.PLAYER)) {
          type = ExecutorType.PLAYER;
          continue;
        } else if (param.asType().toString().equals(Classes.ENTITY)) {
          type = ExecutorType.ENTITY;
          continue;
        }
      }

      arguments.add(param);
    }

    final List<CommandArgument> args = parseArguments(arguments, (TypeElement) element.getEnclosingElement());
    if (args.isEmpty()) {
      populatePathNoArguments(element, thisPath, type, parameters);
      thisPath.setAttribute(AttributeKey.EXECUTOR_TYPE, type);
      debug("  | {}: no arguments found. Current tree for thisPath: {}", transformName(), thisPath);
      return;
    }

    final CommandNode out = root.addChildren(args);
    populatePath(element, out, type, args, parameters);
    out.setAttribute(AttributeKey.EXECUTOR_TYPE, type);
    debug("  | {}: found arguments! Current tree for thisPath: {}", transformName(), thisPath);
  }

  @Override
  public boolean requirement(final ExecutableElement element) {
    return element.getAnnotation(Executes.class) != null;
  }

  @Override
  public BrigadierArgumentConverter argumentConverter() {
    return this.converter;
  }

  @Override
  public final MessagerWrapper delegateMessager() {
    return delegateMessager;
  }
}
