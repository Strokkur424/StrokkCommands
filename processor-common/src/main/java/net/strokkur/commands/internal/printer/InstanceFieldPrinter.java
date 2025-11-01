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
package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceField;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceTypeAnnotation;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.internal.util.Utils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

interface InstanceFieldPrinter<C extends CommandInformation> extends Printable, PrinterInformation<C> {

  default void printInstanceFields() throws IOException {
    if (printInstanceFields(getNode()) > 0) {
      println(); // Extra newline for styling reasons
    }
  }

  private int printInstanceFields(final CommandNode node) throws IOException {
    int pushed = 0;
    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      for (ExecuteAccess<?> executeAccess : node.getAttributeNotNull(AttributeKey.ACCESS_STACK)) {
        if (executeAccess.isRecord()) {
          for (int i = 0; i < pushed; i++) {
            getAccessStack().pop();
          }
          return 0;
        }

        getAccessStack().push(executeAccess);
        pushed++;
      }
    }

    int printed = 0;
    if (node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE) != null) {
      final List<ExecuteAccess<?>> pathToUse;
      if (getAccessStack().size() > 1 && getAccessStack().reversed().get(1) instanceof FieldAccess) {
        pathToUse = getAccessStack().subList(0, getAccessStack().size() - 1);
      } else {
        pathToUse = getAccessStack();
      }

      if (!pathToUse.isEmpty() && printAccessInstance(pathToUse)) {
        printed++;
      }
    }

    for (final CommandNode child : node.children()) {
      printed += printInstanceFields(child);
    }

    for (int i = 0; i < pushed; i++) {
      getAccessStack().pop();
    }

    return printed;
  }

  default String getParameterName(final SourceParameter parameter) {
    return parameter.getName();
  }

  private boolean printAccessInstance(List<ExecuteAccess<?>> accesses) throws IOException {
    if (accesses.isEmpty()) {
      throw new IllegalStateException("Accesses stack was empty");
    }

    if (accesses.size() == 1) {
      if (getPrintedInstances().contains("instance")) {
        return false;
      }
      final String typeName = accesses.getFirst().getSourceName();
      println("final %s%s instance = new %s%s(%s);",
          typeName,
          getCommandInformation().sourceClass().getTypeAnnotations().isEmpty() ?
              "" :
              '<' + String.join(", ", getCommandInformation().sourceClass().getTypeAnnotations().stream()
                  .map(SourceTypeAnnotation::getName)
                  .toList()) + '>',
          typeName,
          getCommandInformation().sourceClass().getTypeAnnotations().isEmpty() ?
              "" :
              "<>",
          String.join(", ", getCommandInformation().constructor() instanceof SourceConstructor ctor ?
              ctor.getParameters().stream()
                  .map(this::getParameterName)
                  .toList() :
              Collections.emptyList()
          )
      );
      getPrintedInstances().add("instance");
      return true;
    }

    final ExecuteAccess<?> currentAccess = accesses.getLast();

    final String typeName = currentAccess.getSourceName();
    final String instanceName = Utils.getInstanceName(accesses);
    final String prevInstanceName = Utils.getInstanceName(accesses.subList(0, accesses.size() - 1));

    if (getPrintedInstances().contains(instanceName)) {
      return false;
    }

    if (currentAccess instanceof FieldAccess fieldAccess) {
      final SourceField fieldElement = fieldAccess.getElement();

      if (!getPrintedInstances().contains(prevInstanceName)) {
        printAccessInstance(accesses.subList(0, accesses.size() - 1));
      }

      if (fieldElement.isInitialized()) {
        println("final {} {} = {}.{};",
            typeName,
            instanceName,
            prevInstanceName,
            fieldAccess.getElement().getName()
        );
      } else {
        println("final {} {} = new {}();",
            typeName,
            instanceName,
            typeName
        );
      }

      getPrintedInstances().add(instanceName);
      return true;
    }

    if (currentAccess instanceof InstanceAccess instanceAccess) {
      final SourceClass classElement = instanceAccess.getElement();
      if (classElement.isTopLevel() || classElement.getModifiers().contains(Modifier.STATIC)) {
        println("final {} {} = new {}();",
            typeName,
            instanceName,
            typeName
        );
        getPrintedInstances().add(instanceName);
        return true;
      }

      if (!getPrintedInstances().contains(prevInstanceName)) {
        printAccessInstance(accesses.subList(0, accesses.size() - 1));
      }

      println("final {} {} = {}.new {}();",
          typeName,
          instanceName,
          prevInstanceName,
          classElement.getName()
      );
      getPrintedInstances().add(instanceName);
      return true;
    }

    throw new IllegalStateException("Unknown access: " + currentAccess);
  }
}
