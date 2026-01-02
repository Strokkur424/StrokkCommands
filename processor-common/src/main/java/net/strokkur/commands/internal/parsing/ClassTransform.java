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
import net.strokkur.commands.internal.NodeUtils;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceField;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;

import java.util.ArrayList;
import java.util.List;

sealed class ClassTransform implements NodeTransform<SourceClass>, ForwardingMessagerWrapper permits RecordTransform {
  protected final CommandParser parser;
  protected final NodeUtils nodeUtils;

  public ClassTransform(final CommandParser parser, final NodeUtils nodeUtils) {
    this.parser = parser;
    this.nodeUtils = nodeUtils;
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
    for (final SourceMethod nestedMethod : element.getNestedMethods()) {
      parser.parseElement(root, nestedMethod);
    }
  }

  @Override
  public final void transform(final CommandNode parent, final SourceClass element) throws MismatchedArgumentTypeException {
    debug("> {}: parsing {}...", transformName(), element);

    final CommandNode node = parseRecordComponents(createSubcommandNode(parent, element), element);
    this.addAccessAttribute(node, ExecuteAccess.of(element));

    this.nodeUtils().applyRegistrableProvider(
        node,
        element,
        nodeUtils().requirementRegistry(),
        AttributeKey.REQUIREMENT_PROVIDER,
        "requirement"
    );
    this.nodeUtils().applyExecutorTransform(node, element);

    parseInnerElements(node, element, this.parser);
  }

  public final void transformWithExecuteAccess(
      final CommandNode parent,
      final SourceClass element,
      final ExecuteAccess<?> access
  ) throws MismatchedArgumentTypeException {
    debug("> {}: parsing {}...", transformName(), element);

    final CommandNode node = parseRecordComponents(createSubcommandNode(parent, element), element);
    this.addAccessAttribute(node, access);

    this.nodeUtils().applyRegistrableProvider(
        node,
        element,
        nodeUtils().requirementRegistry(),
        AttributeKey.REQUIREMENT_PROVIDER,
        "requirement"
    );

    parseInnerElements(node, element, this.parser);
  }

  protected void addAccessAttribute(final CommandNode node, final ExecuteAccess<?> access) {
    node.editAttributeMutable(
        AttributeKey.ACCESS_STACK,
        existing -> existing.add(access),
        () -> new ArrayList<>(List.of(access))
    );
  }

  protected CommandNode parseRecordComponents(final CommandNode parent, final SourceClass element) throws MismatchedArgumentTypeException {
    return parent;
  }

  @Override
  public boolean requirement(final SourceClass element) {
    return element.hasAnnotationInherited(Subcommand.class);
  }

  @Override
  public NodeUtils nodeUtils() {
    return this.nodeUtils;
  }
}
