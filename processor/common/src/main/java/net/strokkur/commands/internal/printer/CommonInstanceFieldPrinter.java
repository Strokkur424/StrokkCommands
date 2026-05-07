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
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.Utils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonInstanceFieldPrinter {
  private final CommonCommandTreePrinter<?> printer;
  private final Set<DefaultExecutable> handledDefaults = new HashSet<>();

  public CommonInstanceFieldPrinter(CommonCommandTreePrinter<?> printer) {
    this.printer = printer;
  }

  public void printInstanceFields() throws IOException {
    if (printInstanceFields(printer.getNode()) > 0) {
      printer.println(); // Extra newline for styling reasons
    }
  }

  public void printInjectedFields() throws IOException {
    if (printInjectedFields(printer.getNode()) > 0) {
      printer.println();
    }
  }

  private List<AccessEntry> collectAccessEntries(CommandNode node) {
    final List<AccessEntry> out = new ArrayList<>();

    int pushed = 0;
    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      for (ExecuteAccess<?> executeAccess : node.getAttributeNotNull(AttributeKey.ACCESS_STACK)) {
        if (executeAccess.isRecord()) {
          for (int i = 0; i < pushed; i++) {
            printer.getAccessStack().pop();
          }
          return out;
        }

        printer.getAccessStack().push(executeAccess);
        pushed++;
      }
    }

    final Executable exec = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    EXEC_NOT_NULL:
    if (exec != null) {
      final List<ExecuteAccess<?>> pathToUse = printer.getAccessStack();

      if (exec instanceof DefaultExecutable defaultExec) {
        if (handledDefaults.contains(defaultExec)) {
          break EXEC_NOT_NULL;
        }
        handledDefaults.add(defaultExec);
      }

      if (!pathToUse.isEmpty()) {
        out.add(new AccessEntry(pathToUse));
      }
    }

    for (CommandNode child : node.children()) {
      out.addAll(collectAccessEntries(child));
    }

    for (int i = 0; i < pushed; i++) {
      printer.getAccessStack().pop();
    }

    return out;
  }

  private int printInstanceFields(CommandNode node) throws IOException {
    int printed = 0;
    for (AccessEntry collectAccessEntry : collectAccessEntries(node)) {
      printed += printAccessInstance(collectAccessEntry);
    }
    return printed;
  }

  private int printInjectedFields(CommandNode node) throws IOException {
    int printed = 0;
    for (AccessEntry collectAccessEntry : collectAccessEntries(node)) {
      printed += printInjectedField(collectAccessEntry);
    }
    return printed;
  }

  public String getParameterName(SourceParameter parameter) {
    return parameter.getName();
  }

  private int printAccessInstance(AccessEntry entry) throws IOException {
    if (printer.getPrintedInstances().contains(entry.getAccessName())) {
      return 0;
    }

    if (entry.isTopLevel()) {
      printer.println("final %s%s instance = new %s%s(%s);",
          entry.getTypeName(), getCtorTypeParameters(),
          entry.getTypeName(), hasCtorTypeParameters() ? "<>" : "",
          getCtorParameters()
      );
      printer.getPrintedInstances().add("instance");
      return 1;
    }

    int printed = 1;
    if (entry.getCurrentAccess() instanceof FieldAccess fieldAccess) {
      printed += printAccessInstance(entry.getParent());

      final SourceField fieldElement = fieldAccess.getElement();
      if (fieldElement.isInitialized()) {
        printer.println("final {} {} = {}.{};",
            entry.getTypeName(), entry.getAccessName(),
            entry.getParent().getAccessName(), fieldAccess.getElement().getName()
        );
      } else {
        printer.println("final {} {} = new {}();",
            entry.getTypeName(), entry.getAccessName(),
            entry.getTypeName()
        );
      }

      printer.getPrintedInstances().add(entry.getAccessName());
      return printed;
    }

    if (entry.getCurrentAccess() instanceof InstanceAccess instanceAccess) {
      final SourceClass classElement = instanceAccess.getElement();
      if (classElement.isTopLevel() || classElement.getModifiers().contains(Modifier.STATIC)) {
        printer.println("final {} {} = new {}();",
            entry.getTypeName(), entry.getAccessName(),
            entry.getTypeName()
        );
      } else {
        printed += printAccessInstance(entry.getParent());
        printer.println("final {} {} = {}.new {}();",
            entry.getTypeName(), entry.getAccessName(),
            entry.getParent().getAccessName(), classElement.getName()
        );
      }

      printer.getPrintedInstances().add(entry.getAccessName());
      return printed;
    }

    throw new IllegalStateException("Unknown access type: " + entry.getCurrentAccess());
  }

  private int printInjectedField(AccessEntry entry) throws IOException {
    if (printer.getPrintedInstances().contains(entry.getAccessName())) {
      return 0;
    }

    if (entry.needsToBeInitialized()) {
      printer.println("private @Inject {} {};",
          entry.getTypeName(), entry.getAccessName()
      );
      printer.getPrintedInstances().add(entry.getAccessName());
      return 1;
    }
    return 0;
  }

  private boolean hasCtorTypeParameters() {
    return !printer.getCommandInformation().sourceClass().getTypeAnnotations().isEmpty();
  }

  private String getCtorTypeParameters() {
    return hasCtorTypeParameters() ?
        '<' + String.join(", ", printer.getCommandInformation().sourceClass().getTypeAnnotations().stream()
            .map(SourceTypeAnnotation::getName)
            .toList()) + '>' :
        "";
  }

  private String getCtorParameters() {
    return String.join(", ", printer.getCommandInformation().constructor() instanceof SourceConstructor ctor ?
        ctor.getParameters().stream()
            .map(this::getParameterName)
            .toList() :
        Collections.emptyList()
    );
  }

  protected record AccessEntry(List<ExecuteAccess<?>> access) {
    protected AccessEntry(List<ExecuteAccess<?>> access) {
      if (access.isEmpty()) {
        throw new IllegalStateException("Access stack cannot be empty.");
      }
      this.access = List.copyOf(access);
    }

    public boolean isTopLevel() {
      return access.size() == 1;
    }

    public String getAccessName() {
      if (isTopLevel()) {
        return "instance";
      }

      return Utils.getInstanceName(access);
    }

    public AccessEntry getParent() {
      return new AccessEntry(access.subList(0, access.size() - 1));
    }

    private ExecuteAccess<?> getCurrentAccess() {
      return access.getLast();
    }

    public String getTypeName() {
      return getCurrentAccess().getSourceName();
    }

    public boolean needsToBeInitialized() {
      final boolean alreadyInitialized = getCurrentAccess() instanceof FieldAccess field && field.getElement().isInitialized();
      return !alreadyInitialized;
    }
  }
}
