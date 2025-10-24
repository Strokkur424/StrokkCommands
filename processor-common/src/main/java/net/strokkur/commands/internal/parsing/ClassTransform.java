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

import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceField;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;

import java.util.ArrayList;
import java.util.List;

sealed class ClassTransform implements NodeTransform<SourceClass>, ForwardingMessagerWrapper permits RecordTransform {
  protected final CommandParser parser;
  protected final PlatformUtils platformUtils;

  public ClassTransform(final CommandParser parser, final PlatformUtils platformUtils) {
    this.parser = parser;
    this.platformUtils = platformUtils;
  }

  protected String transformName() {
    return "ClassTransform";
  }

  static void parseInnerElements(final CommandNode root, final SourceClass element, final CommandParser parser) throws MismatchedArgumentTypeException {
    for (final SourceClass nestedClass : element.getNestedClasses()) {
      parser.parseElement(root, nestedClass);
    }
    for (final SourceField nestedField : element.getNestedFields()) {
      parser.parseElement(root, nestedField);
    }
    for (final SourceClass nestedClass : element.getNestedClasses()) {
      parser.parseElement(root, nestedClass);
    }
  }

  @Override
  public final void transform(final CommandNode parent, final SourceClass element) throws MismatchedArgumentTypeException {
    debug("> {}: parsing {}...", transformName(), element);

    final CommandNode node = parseRecordComponents(createSubcommandNode(parent, element), element);
    this.addAccessAttribute(node, element);

    parseInnerElements(node, element, this.parser);
  }

  protected void addAccessAttribute(final CommandNode node, final SourceClass element) {
    final InstanceAccess access = ExecuteAccess.of(element);
    node.editAttributeMutable(
        AttributeKey.ACCESS_STACK,
        accesses -> accesses.add(access),
        () -> new ArrayList<>(List.of(access))
    );
  }

  protected CommandNode parseRecordComponents(final CommandNode parent, final SourceClass element) throws MismatchedArgumentTypeException {
    return parent;
  }

  @Override
  public boolean requirement(final SourceClass element) {
    return element.getAnnotationOptional(Subcommand.class).isPresent();
  }

  @Override
  public PlatformUtils platformUtils() {
    return this.platformUtils;
  }
}
