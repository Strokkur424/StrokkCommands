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

import net.strokkur.commands.annotations.DefaultExecutes;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.DefaultExecutorArgumentType;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.DefaultExecutablePathImpl;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

final class DefaultExecutesTransform extends ExecutesTransform {

  public DefaultExecutesTransform(final CommandParser parser, final MessagerWrapper delegateMessager) {
    super(parser, delegateMessager);
  }

  private DefaultExecutorArgumentType getType(final TypeMirror type) {
    if (type.toString().equals(Classes.LIST_STRING)) {
      return DefaultExecutorArgumentType.LIST;
    }
    if (type.toString().equals("java.lang.String[]")) {
      return DefaultExecutorArgumentType.ARRAY;
    }
    return DefaultExecutorArgumentType.NONE;
  }

  @Override
  protected String transformName() {
    return "DefaultExecutesTransform";
  }

  @Override
  protected CommandPath<?> createThisPath(final CommandPath<?> parent, final ExecutableElement element) {
    final CommandPath<?> out = this.parser.getLiteralPath(element, DefaultExecutes.class, DefaultExecutes::value);
    return this.populatePath(parent, out, element);
  }

  @Override
  protected int parametersToParse(final List<? extends VariableElement> parameters) {
    final TypeMirror last = parameters.getLast().asType();
    if (getType(last) != DefaultExecutorArgumentType.NONE) {
      return parameters.size() - 1;
    }
    return parameters.size();
  }

  @Override
  protected CommandPath<?> createPath(final ExecutableElement element, final List<CommandArgument> args, final List<? extends VariableElement> parameters) {
    return new DefaultExecutablePathImpl(args, element, getType(parameters.getLast().asType()));
  }

  @Override
  protected CommandPath<?> createNoArgumentsPath(final ExecutableElement element, final List<? extends VariableElement> parameters) {
    return new DefaultExecutablePathImpl(List.of(), element, getType(parameters.getLast().asType()));
  }

  @Override
  public boolean requirement(final ExecutableElement element) {
    return element.getAnnotation(DefaultExecutes.class) != null;
  }
}
