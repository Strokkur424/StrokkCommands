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

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

record FieldTransform(CommandParser parser, MessagerWrapper delegateMessager, BrigadierArgumentConverter argumentConverter) implements NodeTransform<VariableElement>, ForwardingMessagerWrapper {

  @Override
  public void transform(final CommandNode root, final VariableElement element) throws MismatchedArgumentTypeException {
    debug("> FieldTransform: {}.{}", element.getEnclosingElement().getSimpleName(), element.getSimpleName());
    final CommandNode node = createSubcommandNode(root, element);

    node.setAttribute(AttributeKey.ACCESS_STACK, new ArrayList<>(List.of(ExecuteAccess.of(element))));

    this.parser.parseClass(node, (TypeElement) StrokkCommandsProcessor.getTypes().asElement(element.asType()));
  }

  @Override
  public boolean requirement(final VariableElement element) {
    return element.getAnnotation(Command.class) != null || element.getAnnotation(Subcommand.class) != null;
  }
}
