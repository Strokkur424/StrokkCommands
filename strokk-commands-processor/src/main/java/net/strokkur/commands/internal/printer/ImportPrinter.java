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

import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.Utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

interface ImportPrinter extends Printable, PrinterInformation {

  Set<String> STANDARD_IMPORTS = Set.of(
      Classes.COMMAND,
      Classes.LITERAL_COMMAND_NODE,
      Classes.COMMAND_SOURCE_STACK,
      Classes.COMMANDS,
      Classes.LIST,
      Classes.NULL_MARKED
  );

  default void printImports(Set<String> imports) throws IOException {
    final Map<Boolean, List<String>> splitImports = imports.stream()
        .sorted()
        .collect(Collectors.partitioningBy(str -> str.startsWith("java")));

    final List<String> javaImports = splitImports.get(true);
    final List<String> otherImports = splitImports.get(false);

    for (String i : otherImports) {
      println("import {};", i);
    }

    println();

    for (String i : javaImports) {
      println("import {};", i);
    }
  }

  default Set<String> getImports() {
    final Set<String> imports = new HashSet<>(STANDARD_IMPORTS);
    gatherImports(imports, getNode());

    imports.removeIf(importString -> {
      if (importString.startsWith("java.lang")) {
        return true;
      }

      final TypeElement element = StrokkCommandsProcessor.getElements().getTypeElement(importString);
      if (element == null) {
        return false;
      }

      return Utils.getPackageElement(element) == Utils.getPackageElement(getCommandInformation().classElement());
    });

    return imports;
  }

  private void gatherImports(Set<String> imports, CommandNode node) {
    if (node.hasAttribute(AttributeKey.DEFAULT_EXECUTABLE)) {
      imports.addAll(node.getAttributeNotNull(AttributeKey.DEFAULT_EXECUTABLE).defaultExecutableArgumentTypes().getImports());
    }

    if (getCommandInformation().constructor() instanceof ExecutableElement ctor) {
      Utils.populateParameterImports(imports, ctor);
      for (final VariableElement param : ctor.getParameters()) {
        Utils.populateParameterImports(imports, param);
      }
      for (final Element typeParam : ctor.getTypeParameters()) {
        Utils.populateParameterImports(imports, typeParam);
      }
    }
    for (final TypeParameterElement typeParameter : getCommandInformation().classElement().getTypeParameters()) {
      Utils.populateParameterImports(imports, typeParameter);
    }

    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      for (final ExecuteAccess<?> access : node.getAttributeNotNull(AttributeKey.ACCESS_STACK)) {
        if (access instanceof InstanceAccess instanceAccess) {
          imports.add(instanceAccess.getElement().getQualifiedName().toString());
        } else if (access instanceof FieldAccess fieldAccess) {
          imports.add(((TypeElement) StrokkCommandsProcessor.getTypes().asElement(fieldAccess.getElement().asType())).getQualifiedName().toString());
        }
      }
    }

    if (node.argument() instanceof RequiredCommandArgument req) {
      imports.addAll(req.getArgumentType().imports());

      if (req.getSuggestionProvider() != null) {
        final TypeElement suggestionsTypeElement = req.getSuggestionProvider().getClassElement();
        if (suggestionsTypeElement != null) {
          imports.add(suggestionsTypeElement.getQualifiedName().toString());
        }
      }
    }

    final ExecutorType executorType = node.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);
    if (executorType == ExecutorType.PLAYER) {
      imports.add(Classes.PLAYER);
    } else if (executorType == ExecutorType.ENTITY) {
      imports.add(Classes.ENTITY);
    }

    if (executorType != ExecutorType.NONE) {
      imports.add(Classes.SIMPLE_COMMAND_EXCEPTION_TYPE);
      imports.add(Classes.MESSAGE_COMPONENT_SERIALIZER);
      imports.add(Classes.COMPONENT);
    }

    for (final CommandNode child : node.children()) {
      gatherImports(imports, child);
    }
  }
}
