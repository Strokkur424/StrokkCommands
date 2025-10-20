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
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

sealed class ClassTransform implements NodeTransform<TypeElement>, ForwardingMessagerWrapper permits RecordTransform {
  private static final Set<ElementKind> ENCLOSED_ELEMENTS_TO_PARSE = Set.of(
      ElementKind.METHOD,
      ElementKind.FIELD,
      ElementKind.CLASS,
      ElementKind.RECORD
  );

  protected final CommandParser parser;
  private final MessagerWrapper messager;
  private final BrigadierArgumentConverter converter;

  public ClassTransform(final CommandParser parser, final MessagerWrapper messager, final BrigadierArgumentConverter converter) {
    this.parser = parser;
    this.messager = messager;
    this.converter = converter;
  }

  protected String transformName() {
    return "ClassTransform";
  }

  static void parseInnerElements(final CommandNode root, final TypeElement element, final CommandParser parser) throws MismatchedArgumentTypeException {
    for (final Element enclosed : element.getEnclosedElements()) {
      if (!ENCLOSED_ELEMENTS_TO_PARSE.contains(enclosed.getKind())) {
        continue;
      }

      parser.parseElement(root, enclosed);
    }
  }

  @Override
  public final void transform(final CommandNode parent, final TypeElement element) throws MismatchedArgumentTypeException {
    debug("> {}: parsing {}...", transformName(), element);

    final CommandNode node = this.createSubcommandNode(parent, element);
    addAccessAttribute(node, element);

    parseInnerElements(parseRecordComponents(node, element), element, this.parser);
  }

  protected void addAccessAttribute(final CommandNode node, final TypeElement element) {
    final InstanceAccess access = ExecuteAccess.of(element);
    node.editAttributeMutable(
        AttributeKey.ACCESS_STACK,
        accesses -> accesses.add(access),
        () -> new ArrayList<>(List.of(access))
    );
  }

  protected CommandNode parseRecordComponents(final CommandNode parent, final TypeElement element) throws MismatchedArgumentTypeException {
    return parent;
  }

  @Override
  public boolean requirement(final TypeElement element) {
    return element.getAnnotation(Command.class) != null || element.getAnnotation(Subcommand.class) != null;
  }

  @Override
  public BrigadierArgumentConverter argumentConverter() {
    return this.converter;
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return this.messager;
  }
}
