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

import net.strokkur.commands.Subcommand;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceField;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;

import java.util.ArrayList;
import java.util.List;

record FieldTransform(CommandParser parser, PlatformUtils platformUtils) implements NodeTransform<SourceField>, ForwardingMessagerWrapper {

  @Override
  public void transform(final CommandNode root, final SourceField element) throws MismatchedArgumentTypeException {
    debug("> FieldTransform: {}.{}", element.getEnclosed().getName(), element.getName());
    final CommandNode node = createSubcommandNode(root, element);

    node.editAttributeMutable(
        AttributeKey.ACCESS_STACK,
        list -> list.add(ExecuteAccess.of(element)),
        () -> new ArrayList<>(List.of(ExecuteAccess.of(element)))
    );

    this.parser.parseClass(node, (SourceClass) element.getType());
  }

  @Override
  public boolean requirement(final SourceField element) {
    return element.getAnnotationOptional(Subcommand.class).isPresent() && element.getType() instanceof SourceClass;
  }
}
