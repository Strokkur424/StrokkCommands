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

import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class CommandParserImpl implements CommandParser, ForwardingMessagerWrapper {
  private final NodeTransform<TypeElement> classTransform;
  private final NodeTransform<TypeElement> recordTransform;
  private final NodeTransform<ExecutableElement> methodTransform;
  private final NodeTransform<VariableElement> fieldTransform;

  private final MessagerWrapper messager;

  public CommandParserImpl(final MessagerWrapper messager, final BrigadierArgumentConverter converter) {
    this.messager = messager;

    this.classTransform = new ClassTransform(this, messager, converter);
    this.recordTransform = new RecordTransform(this, messager, converter);
    this.methodTransform = new MethodTransform(this, messager, converter);
    this.fieldTransform = new FieldTransform(this, messager, converter);
  }

  @Override
  public CommandNode createCommandTree(final String name, final TypeElement typeElement) {
    final CommandNode root = CommandNode.createRoot(LiteralCommandArgument.literal(name, typeElement));
    try {
      ClassTransform.parseInnerElements(root, typeElement, this);
    } catch (MismatchedArgumentTypeException e) {
      // TODO: replace with more sophisticated logging
      throw new RuntimeException(e);
    }
    return root;
  }

  @Override
  public void parseElement(final CommandNode node, final Element element) {
    try {
      switch (element) {
        case TypeElement type -> {
          if (type.getKind() == ElementKind.RECORD) {
            this.recordTransform.transformIfRequirement(node, type);
          } else {
            this.classTransform.transformIfRequirement(node, type);
          }
        }
        case ExecutableElement method -> this.methodTransform.transformIfRequirement(node, method);
        case VariableElement var -> this.fieldTransform.transformIfRequirement(node, var);
        default -> {
        }
      }
    } catch (MismatchedArgumentTypeException ex) {
      // TODO: Replace with more sophisticated handling
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void parseClass(final CommandNode node, final TypeElement element) throws MismatchedArgumentTypeException {
    if (element.getKind() == ElementKind.RECORD) {
      this.recordTransform.transform(node, element);
    } else if (element.getKind() == ElementKind.CLASS) {
      this.classTransform.transform(node, element);
    } else {
      throw new IllegalStateException("Unknown class type: " + element.getKind().name());
    }
  }

  @Override
  public void parseMethod(final CommandNode node, final ExecutableElement element) throws MismatchedArgumentTypeException {
    this.methodTransform.transform(node, element);
  }

  @Override
  public void parseField(final CommandNode node, final VariableElement element) throws MismatchedArgumentTypeException {
    if (element.getKind() == ElementKind.FIELD) {
      this.fieldTransform.transform(node, element);
    } else {
      this.infoElement("Tried to parse variable elements as field", element);
    }
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return messager;
  }
}
