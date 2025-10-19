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
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePathImpl;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

sealed class ExecutesTransform implements PathTransform<ExecutableElement>, ForwardingMessagerWrapper permits DefaultExecutesTransform {
  protected final CommandParser parser;
  private final MessagerWrapper delegateMessager;

  protected ExecutesTransform(CommandParser parser, MessagerWrapper delegateMessager) {
    this.parser = parser;
    this.delegateMessager = delegateMessager;
  }

  protected String transformName() {
    return "ExecutesTransform";
  }

  protected CommandPath<?> createThisPath(final CommandPath<?> parent, final ExecutableElement element) {
    return this.createThisExecutesPath(parent, this.parser, element);
  }

  protected int parametersToParse(final List<? extends VariableElement> parameters) {
    return parameters.size();
  }

  protected CommandPath<?> createPath(final ExecutableElement element, final List<CommandArgument> args, final List<? extends VariableElement> parameters) {
    return new ExecutablePathImpl(args, element);
  }

  protected CommandPath<?> createNoArgumentsPath(final ExecutableElement element, final List<? extends VariableElement> parameters) {
    return new ExecutablePathImpl(List.of(), element);
  }

  @Override
  public final void transform(final CommandPath<?> parent, final ExecutableElement element) {
    debug("> {}: parsing {} for '{}'", transformName(), element, parent.toStringNoChildren());
    final CommandPath<?> thisPath = this.createThisPath(parent, element);

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

    final List<List<CommandArgument>> args = this.parser.parseArguments(arguments, (TypeElement) element.getEnclosingElement());
    if (args.isEmpty()) {
      final CommandPath<?> out = createNoArgumentsPath(element, parameters);
      out.setAttribute(AttributeKey.EXECUTOR_TYPE, type);
      thisPath.addChild(out);
      debug("> {}: no arguments found. Current tree for thisPath: {}", transformName(), thisPath);
      return;
    }

    for (final List<CommandArgument> argList : args) {
      final CommandPath<?> out = createPath(element, argList, parameters);
      out.setAttribute(AttributeKey.EXECUTOR_TYPE, type);
      thisPath.addChild(out);
    }
    debug("> {}: found arguments! Current tree for thisPath: {}", transformName(), thisPath);
  }

  @Override
  public boolean requirement(final ExecutableElement element) {
    return element.getAnnotation(Executes.class) != null;
  }

  @Override
  public final MessagerWrapper delegateMessager() {
    return delegateMessager;
  }
}
