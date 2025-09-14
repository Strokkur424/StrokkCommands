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

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPath;
import net.strokkur.commands.internal.intermediate.paths.RecordPathImpl;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

class RecordTransform extends ClassTransform {

  public RecordTransform(final CommandParser parser, final MessagerWrapper messager) {
    super(parser, messager);
  }

  @Override
  protected void addAccessAttribute(final CommandPath<?> path, final TypeElement element) {
    // no impl
  }

  @Override
  protected List<CommandPath<?>> parseRecordComponents(final CommandPath<?> parent, final Element element) {
    final List<? extends Element> enclosedElements = element.getEnclosedElements();

    final List<VariableElement> recordComponents = new ArrayList<>(enclosedElements.size());
    for (final Element enclosed : enclosedElements) {
      if (enclosed.getKind() == ElementKind.RECORD_COMPONENT) {
        recordComponents.add((VariableElement) enclosed);
      }
    }

    final List<List<CommandArgument>> possibleArguments = this.parser.parseArguments(recordComponents, (TypeElement) element);
    final List<CommandPath<?>> paths = new ArrayList<>(possibleArguments.size());

    for (final List<CommandArgument> arguments : possibleArguments) {
      final RecordPath recordPath = new RecordPathImpl(arguments);
      parent.addChild(recordPath);
      paths.add(recordPath);
    }

    return paths;
  }

  @Override
  public boolean hardRequirement(final Element element) {
    return element.getKind() == ElementKind.RECORD;
  }
}
