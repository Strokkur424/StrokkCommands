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
import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.RequiresOP;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import java.util.Set;

interface NodeTransform<S extends Element> extends PathUtils {

  void transform(CommandNode parent, S element) throws MismatchedArgumentTypeException;

  boolean requirement(S element);

  default void transformIfRequirement(CommandNode parent, S element) throws MismatchedArgumentTypeException {
    if (requirement(element)) {
      transform(parent, element);
    }
  }

  default CommandNode createSubcommandNode(CommandNode parent, Element element) {
    final CommandNode node = createLiteralSequence(parent, element, Subcommand.class, Subcommand::value);
    return populateNode(parent, node, element);
  }

  default CommandNode createExecutesNode(CommandNode parent, Element element) {
    CommandNode thisPath = createLiteralSequence(parent, element, Executes.class, Executes::value);
    return populateNode(parent, thisPath, element);
  }

  default CommandNode populateNode(final CommandNode parent, final @Nullable CommandNode thisPath, final Element element) {
    final CommandNode out = thisPath == null ? parent : thisPath;

    // Add permission and RequiresOP clauses
    final Permission permission = element.getAnnotation(Permission.class);
    if (permission != null) {
      out.editAttributeMutable(AttributeKey.PERMISSIONS, s -> s.add(permission.value()), () -> Set.of(permission.value()));
    }

    if (element.getAnnotation(RequiresOP.class) != null) {
      out.setAttribute(AttributeKey.REQUIRES_OP, true);
    }

    return out;
  }
}
