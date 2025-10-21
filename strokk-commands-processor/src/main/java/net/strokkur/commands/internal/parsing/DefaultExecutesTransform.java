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
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutableImpl;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

final class DefaultExecutesTransform extends ExecutesTransform {

  DefaultExecutesTransform(final CommandParser parser, final MessagerWrapper delegateMessager, final BrigadierArgumentConverter converter) {
    super(parser, delegateMessager, converter);
  }

  private DefaultExecutable.Type getType(final TypeMirror type) {
    if (type.toString().equals(Classes.LIST_STRING)) {
      return DefaultExecutable.Type.LIST;
    }
    if (type.toString().equals("java.lang.String[]")) {
      return DefaultExecutable.Type.ARRAY;
    }
    return DefaultExecutable.Type.NONE;
  }

  @Override
  protected String transformName() {
    return "DefaultExecutesTransform";
  }

  @Override
  protected CommandNode createThisPath(final CommandNode parent, final ExecutableElement element) {
    final CommandNode out = createLiteralSequence(parent, element, DefaultExecutes.class, DefaultExecutes::value);
    return this.populateNode(parent, out, element);
  }

  @Override
  protected int parametersToParse(final List<? extends VariableElement> parameters) {
    final TypeMirror last = parameters.getLast().asType();
    if (getType(last) != DefaultExecutable.Type.NONE) {
      return parameters.size() - 1;
    }
    return parameters.size();
  }

  @Override
  protected void populatePath(
      final ExecutableElement method,
      final CommandNode node,
      final ExecutorType type,
      final List<CommandArgument> args,
      final List<? extends VariableElement> parameters) {
    node.setAttribute(AttributeKey.DEFAULT_EXECUTABLE, new DefaultExecutableImpl(type, method, args, getType(parameters.getLast().asType())));
  }

  @Override
  protected void populatePathNoArguments(
      final ExecutableElement method,
      final CommandNode node,
      final ExecutorType type,
      final List<? extends VariableElement> parameters) {
    node.setAttribute(AttributeKey.DEFAULT_EXECUTABLE, new DefaultExecutableImpl(type, method, List.of(), getType(parameters.getLast().asType())));
  }

  @Override
  public boolean requirement(final ExecutableElement element) {
    return element.getAnnotation(DefaultExecutes.class) != null;
  }
}
