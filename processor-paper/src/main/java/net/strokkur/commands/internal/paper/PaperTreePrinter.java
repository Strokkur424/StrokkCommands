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
package net.strokkur.commands.internal.paper;

import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.exceptions.PrinterException;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.ExecutorType;
import net.strokkur.commands.internal.paper.util.PaperAttributeKeys;
import net.strokkur.commands.internal.paper.util.PaperClasses;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.printer.CommonTreePrinter;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.paper.Executor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class PaperTreePrinter extends CommonTreePrinter {
  public PaperTreePrinter(final CommonCommandTreePrinter<?> printer) {
    super(printer);
  }

  @Override
  public void prefixPrintExecutableInner(final CommandNode node, final Executable executable) throws IOException {
    final ExecutorType executorType = executable.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);
    if (executorType != ExecutorType.NONE) {
      printer.printBlock("""
              if (!(ctx.getSource().getExecutor() instanceof %s executor)) {
                  throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                      Component.text("This command requires %s %s executor!")
                  )).create();
              }""",
          executorType.toString().charAt(0) + executorType.toString().toLowerCase().substring(1),
          executorType == ExecutorType.ENTITY ? "an" : "a",
          executorType.toString().toLowerCase()
      );
      printer.println();
    }
  }

  @Override
  public String handleParameter(final SourceVariable parameter) throws IOException {
    if (parameter.hasAnnotationInherited(Executor.class)) {
      return "executor";
    }

    if (parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(Classes.COMMAND_CONTEXT + "<" + PaperClasses.COMMAND_SOURCE_STACK + ">")) {
      return "ctx";
    }

    if (parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(PaperClasses.COMMAND_SOURCE_STACK)) {
      return "ctx.getSource()";
    }

    if (parameter.getType().getFullyQualifiedName().equalsIgnoreCase(PaperClasses.COMMAND_SENDER)) {
      return "ctx.getSource().getSender()";
    }

    final DefaultExecutable.Type type = DefaultExecutable.Type.getType(parameter);
    if (type == DefaultExecutable.Type.LIST || type == DefaultExecutable.Type.ARRAY) {
      return Objects.requireNonNull(type.getGetter());
    }

    throw new PrinterException("Unknown parameter type: " + parameter.getName());
  }

  @Override
  public @Nullable String getExtraRequirements(final Attributable node) {
    final List<String> extraRequirements = new ArrayList<>();

    final ExecutorType executorType = node.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);
    if (executorType != ExecutorType.NONE) {
      extraRequirements.add(executorType.getPredicate());
    }

    final boolean operator = node.getAttributeNotNull(PaperAttributeKeys.REQUIRES_OP);
    if (operator) {
      extraRequirements.add("source.getSender().isOp()");
    }

    final List<String> permissions = node.getAttributeNotNull(PaperAttributeKeys.PERMISSIONS).stream()
        .map("source.getSender().hasPermission(\"%s\")"::formatted)
        .toList();

    if (!permissions.isEmpty()) {
      if (permissions.size() == 1) {
        extraRequirements.add(permissions.getFirst());
      } else {
        extraRequirements.add('(' + String.join(" || ", permissions) + ')');
      }
    }

    return extraRequirements.isEmpty() ? null : String.join(" && ", extraRequirements);
  }

  @Override
  public String getLiteralMethodString() {
    return "Commands.literal";
  }

  @Override
  public String getArgumentMethodString() {
    return "Commands.argument";
  }
}
