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
import net.strokkur.commands.internal.abstraction.SourceElement;
import net.strokkur.commands.internal.abstraction.SourceField;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceRecord;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class CommandParserImpl implements CommandParser, ForwardingMessagerWrapper {
  private final ClassTransform classTransform;
  private final RecordTransform recordTransform;
  private final NodeTransform<SourceMethod> methodTransform;
  private final NodeTransform<SourceField> fieldTransform;

  private final MessagerWrapper messager;
  private final NodeUtils nodeUtils;

  public CommandParserImpl(
      final MessagerWrapper messager,
      final NodeUtils nodeUtils,
      final Function<CommandParser, ExecutesTransform> executesTransform,
      final Function<CommandParser, DefaultExecutesTransform> defaultExecutesTransform
  ) {
    this.messager = messager;
    this.nodeUtils = nodeUtils;

    this.classTransform = new ClassTransform(this, nodeUtils);
    this.recordTransform = new RecordTransform(this, nodeUtils);
    this.methodTransform = new MethodTransform(nodeUtils, executesTransform.apply(this), defaultExecutesTransform.apply(this));
    this.fieldTransform = new FieldTransform(this, nodeUtils);
  }

  @Override
  public @Nullable CommandNode createCommandTree(final String name, final SourceClass sourceClass) {
    final CommandNode root = CommandNode.createRoot(LiteralCommandArgument.literal(name, sourceClass));
    try {
      final ClassTransform transform = sourceClass.isRecord() ? this.recordTransform : this.classTransform;
      final CommandNode node = transform.parseRecordComponents(root, sourceClass);
      nodeUtils.applyRegistrableProvider(
          node,
          sourceClass,
          nodeUtils.requirementRegistry(),
          AttributeKey.REQUIREMENT_PROVIDER,
          "requirement"
      );
      transform.populateNode(null, node, sourceClass);
      transform.addAccessAttribute(node, sourceClass);
      ClassTransform.parseInnerElements(node, sourceClass, this);
    } catch (MismatchedArgumentTypeException e) {
      errorSource(e.getMessage(), sourceClass);
    }
    return root;
  }

  @Override
  public void parseElement(final CommandNode node, final SourceElement element) {
    try {
      switch (element) {
        case SourceRecord sourceRecord -> this.recordTransform.transformIfRequirement(node, sourceRecord);
        case SourceClass sourceClass -> this.classTransform.transformIfRequirement(node, sourceClass);
        case SourceMethod method -> this.methodTransform.transformIfRequirement(node, method);
        case SourceField field -> this.fieldTransform.transformIfRequirement(node, field);
        default -> {
        }
      }
    } catch (MismatchedArgumentTypeException | UnknownSenderException ex) {
      errorSource(ex.getMessage(), element);
    }
  }

  @Override
  public void parseClass(final CommandNode node, final SourceClass sourceClass) throws MismatchedArgumentTypeException {
    if (sourceClass.isRecord()) {
      this.recordTransform.transform(node, sourceClass);
    } else {
      this.classTransform.transform(node, sourceClass);
    }
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return messager;
  }
}
