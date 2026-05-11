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
package net.strokkur.commands.internal.velocity;

import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.exceptions.PrinterException;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonClassBuilder;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

final class VelocityClassBuilder extends CommonClassBuilder {
  VelocityClassBuilder(CommonCommandTreePrinter<?> printer) {
    super(printer);
  }

  @Override
  public void prefixPrintExecutableInner(CommandNode node, Executable executable) throws IOException {
    final SenderType type = executable.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE);
    if (type != SenderType.NORMAL) {
      printer.printBlock("""
              if (!(ctx.getSource() instanceof %s source)) {
                  throw new SimpleCommandExceptionType(
                      new LiteralMessage("This command requires a %s sender!")
                  ).create();
              }""",
          List.of(type.getClassName().split("\\.")).getLast(),
          type.toString().toLowerCase(Locale.ROOT)
      );
      printer.println();
    }
  }

  @Override
  public String handleParameter(SourceVariable parameter) throws IOException {
    if (parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(Classes.COMMAND_CONTEXT + "<" + VelocityClasses.COMMAND_SOURCE + ">")) {
      return "ctx";
    }

    if (parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(VelocityClasses.COMMAND_SOURCE)) {
      return "ctx.getSource()";
    }

    if (parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(VelocityClasses.PLAYER)
        || parameter.getType().getFullyQualifiedAndTypedName().equalsIgnoreCase(VelocityClasses.CONSOLE_COMMAND_SOURCE)) {
      return "source";
    }

    final DefaultExecutable.Type type = DefaultExecutable.Type.getType(parameter);
    if (type == DefaultExecutable.Type.LIST || type == DefaultExecutable.Type.ARRAY) {
      return Objects.requireNonNull(type.getGetter());
    }

    throw new PrinterException("Unknown parameter type: " + parameter.getName());
  }

  @Override
  public @Nullable String getExtraRequirements(Attributable node) {
    final Set<String> permissions = node.getAttributeNotNull(VelocityAttributeKeys.PERMISSIONS);
    final SenderType type = node.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE);

    if (type == SenderType.NORMAL) {
      if (permissions.isEmpty()) {
        return null;
      }

      return String.join(" || ", permissions.stream()
          .map(perm -> "source.hasPermission(\"" + perm + "\")")
          .toList());
    }

    if (permissions.isEmpty()) {
      return type.getPredicate();
    }
    if (permissions.size() == 1) {
      return "%s && source.hasPermission(\"%s\")".formatted(
          type.getPredicate(),
          permissions.stream().findFirst().get()
      );
    }

    return "source -> %s && (%s))".formatted(
        type.getPredicate(),
        String.join(" || ", permissions.stream()
            .map(perm -> "source.hasPermission(\"" + perm + "\")")
            .toList())
    );
  }

  @Override
  public String getLiteralMethodString() {
    return "BrigadierCommand.literalArgumentBuilder";
  }

  @Override
  public String getArgumentMethodString() {
    return "BrigadierCommand.requiredArgumentBuilder";
  }
}
