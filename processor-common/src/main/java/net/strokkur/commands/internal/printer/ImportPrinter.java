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

import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceTypeAnnotation;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.CommandInformation;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

interface ImportPrinter<C extends CommandInformation> extends Printable, PrinterInformation<C> {

  Set<String> standardImports();

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
    final Set<String> imports = new HashSet<>(standardImports());
    gatherImports(imports, getNode());

    final String sourceClassFqn = getCommandInformation().sourceClass().getFullyQualifiedName();
    final int numberOfDots = sourceClassFqn.split("\\.").length;
    imports.removeIf(importString -> {
      if (importString.startsWith("java.lang")) {
        return true;
      }

      if (!importString.startsWith(sourceClassFqn)) {
        return false;
      }

      return importString.split("\\.").length == numberOfDots;
    });

    return imports;
  }

  void gatherAdditionalArgumentImports(Set<String> imports, RequiredCommandArgument argument);

  void gatherAdditionalNodeImports(Set<String> imports, CommandNode node);

  private void gatherImports(final Set<String> imports, final CommandNode node) {
    if (node.hasAttribute(AttributeKey.DEFAULT_EXECUTABLE)) {
      imports.addAll(node.getAttributeNotNull(AttributeKey.DEFAULT_EXECUTABLE).defaultExecutableArgumentTypes().getImports());
    }

    if (getCommandInformation().constructor() instanceof SourceConstructor ctor) {
      imports.addAll(ctor.getImports());
    }
    for (final SourceTypeAnnotation typeParameter : getCommandInformation().sourceClass().getTypeAnnotations()) {
      imports.addAll(typeParameter.getImports());
    }

    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      for (final ExecuteAccess<?> access : node.getAttributeNotNull(AttributeKey.ACCESS_STACK)) {
        if (access instanceof InstanceAccess instanceAccess) {
          imports.addAll(instanceAccess.getElement().getImports());
        } else if (access instanceof FieldAccess fieldAccess) {
          imports.addAll(fieldAccess.getElement().getImports());
        }
      }
    }

    if (node.argument() instanceof RequiredCommandArgument req) {
      imports.addAll(req.argumentType().imports());
      gatherAdditionalArgumentImports(imports, req);
    }

    gatherAdditionalNodeImports(imports, node);

    for (final CommandNode child : node.children()) {
      gatherImports(imports, child);
    }
  }
}
