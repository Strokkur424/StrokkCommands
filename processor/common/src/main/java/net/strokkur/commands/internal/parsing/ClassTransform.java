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

  ClassTransform(CommandParser parser, NodeUtils nodeUtils) {
    this.parser = parser;
    this.nodeUtils = nodeUtils;
  }

  protected String transformName() {
    return "ClassTransform";
  }

  static void parseInnerElements(CommandNode root, SourceClass element, CommandParser parser) throws MismatchedArgumentTypeException {
    for (SourceClass nestedClass : element.getNestedClasses()) {
      parser.parseElement(root, nestedClass);
    }
    for (SourceField nestedField : element.getNestedFields()) {
      parser.parseElement(root, nestedField);
    }
    for (SourceMethod nestedMethod : element.getNestedMethods()) {
      parser.parseElement(root, nestedMethod);
    }
  }

  @Override
  public final void transform(CommandNode parent, SourceClass element) throws MismatchedArgumentTypeException {
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

  public final void transformWithExecuteAccess(CommandNode parent, SourceClass element, ExecuteAccess<?> access)
      throws MismatchedArgumentTypeException {
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

  protected void addAccessAttribute(CommandNode node, ExecuteAccess<?> access) {
    node.editAttributeMutable(
        AttributeKey.ACCESS_STACK,
        existing -> existing.add(access),
        () -> new ArrayList<>(List.of(access))
    );
  }

  protected CommandNode parseRecordComponents(CommandNode parent, SourceClass element) throws MismatchedArgumentTypeException {
    return parent;
  }

  @Override
  public boolean requirement(SourceClass element) {
    return element.hasAnnotationInherited(Subcommand.class);
  }

  @Override
  public NodeUtils nodeUtils() {
    return this.nodeUtils;
  }
}
