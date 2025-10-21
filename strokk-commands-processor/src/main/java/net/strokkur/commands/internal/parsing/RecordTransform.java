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
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.ParameterizableImpl;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

final class RecordTransform extends ClassTransform {

  public RecordTransform(final CommandParser parser, final MessagerWrapper messager, final BrigadierArgumentConverter converter) {
    super(parser, messager, converter);
  }

  @Override
  protected String transformName() {
    return "RecordTransform";
  }

  @Override
  protected void addAccessAttribute(final CommandNode path, final TypeElement element) {
    // no impl
  }

  @Override
  protected CommandNode parseRecordComponents(final CommandNode parent, final TypeElement element) throws MismatchedArgumentTypeException {
    final List<? extends Element> enclosedElements = element.getEnclosedElements();

    final List<VariableElement> recordComponents = new ArrayList<>(enclosedElements.size());
    for (final Element enclosed : enclosedElements) {
      if (enclosed.getKind() == ElementKind.RECORD_COMPONENT) {
        recordComponents.add((VariableElement) enclosed);
      }
    }

    final List<CommandArgument> arguments = parseArguments(recordComponents, element);
    final CommandNode out = parent.addChildren(arguments);

    out.setAttribute(AttributeKey.RECORD_ARGUMENTS, new ParameterizableImpl(arguments));

    return out;
  }
}
