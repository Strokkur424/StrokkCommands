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

import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.printer.CommonImportPrinter;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;

import java.util.Set;

final class VelocityImportPrinter extends CommonImportPrinter {
  public VelocityImportPrinter(final CommonCommandTreePrinter<?> printer) {
    super(printer);
  }

  @Override
  public Set<String> standardImports() {
    return Set.of(
        Classes.COMMAND,
        VelocityClasses.LITERAL_ARGUMENT_BUILDER,
        VelocityClasses.BRIGADIER_COMMAND,
        VelocityClasses.COMMAND_META,
        VelocityClasses.COMMAND_SOURCE,
        VelocityClasses.PROXY_INITIALIZE_EVENT,
        VelocityClasses.PROXY_SERVER,
        Classes.NULL_MARKED,
        Classes.LIST
    );
  }

  @Override
  public void gatherAdditionalNodeImports(final Set<String> imports, final CommandNode node) {
    addExecutorTypeImports(imports, node.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE));
    final Executable executable = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      addExecutorTypeImports(imports, executable.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE));
    }
  }

  private void addExecutorTypeImports(final Set<String> imports, final SenderType type) {
    if (type == SenderType.NORMAL) {
      return;
    }

    if (type == SenderType.PLAYER) {
      imports.add(VelocityClasses.PLAYER);
    } else if (type == SenderType.CONSOLE) {
      imports.add(VelocityClasses.CONSOLE_COMMAND_SOURCE);
    }

    imports.add(Classes.SIMPLE_COMMAND_EXCEPTION_TYPE);
    imports.add(Classes.LITERAL_MESSAGE);
  }
}
