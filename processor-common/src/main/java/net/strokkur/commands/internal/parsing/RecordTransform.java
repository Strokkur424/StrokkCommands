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

import net.strokkur.commands.internal.NodeUtils;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceRecord;
import net.strokkur.commands.internal.abstraction.SourceRecordComponent;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.ParameterType;
import net.strokkur.commands.internal.intermediate.executable.Parameterizable;
import net.strokkur.commands.internal.intermediate.executable.ParameterizableImpl;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;

import java.util.List;

final class RecordTransform extends ClassTransform {

  public RecordTransform(final CommandParser parser, final NodeUtils nodeUtils) {
    super(parser, nodeUtils);
  }

  @Override
  protected String transformName() {
    return "RecordTransform";
  }

  @Override
  protected void addAccessAttribute(final CommandNode node, final ExecuteAccess<?> accesses) {
    // no impl -- records are always instantiated statically
  }

  @Override
  protected CommandNode parseRecordComponents(final CommandNode parent, final SourceClass element) throws MismatchedArgumentTypeException {
    final List<SourceRecordComponent> components = ((SourceRecord) element).getRecordComponents();

    final List<ParameterType> arguments = components.stream()
        .map(nodeUtils()::parseParameter)
        .toList();
    final CommandNode out = parent.addChildren(arguments.stream()
        .filter(CommandArgument.class::isInstance)
        .map(CommandArgument.class::cast)
        .toList());

    out.setAttribute(AttributeKey.RECORD_ARGUMENTS, new ParameterizableImpl(arguments));

    return out;
  }
}
