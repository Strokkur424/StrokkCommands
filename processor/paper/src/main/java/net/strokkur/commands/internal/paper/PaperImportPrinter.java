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

import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.ExecutorType;
import net.strokkur.commands.internal.paper.util.PaperAttributeKeys;
import net.strokkur.commands.internal.paper.util.PaperClasses;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.printer.CommonImportPrinter;
import net.strokkur.commands.internal.util.Classes;

import java.util.Set;

final class PaperImportPrinter extends CommonImportPrinter {
  PaperImportPrinter(CommonCommandTreePrinter<?> printer) {
    super(printer);
  }

  @Override
  public Set<String> standardImports() {
    return Set.of(
        Classes.COMMAND,
        Classes.LITERAL_COMMAND_NODE,
        PaperClasses.COMMAND_SOURCE_STACK,
        PaperClasses.COMMANDS,
        Classes.LIST,
        Classes.NULL_MARKED,
        Classes.NULLABLE,
        "io.papermc.paper.plugin.bootstrap.PluginBootstrap",
        "io.papermc.paper.plugin.bootstrap.BootstrapContext",
        "org.bukkit.plugin.java.JavaPlugin"
    );
  }

  @Override
  public void gatherAdditionalNodeImports(Set<String> imports, CommandNode node) {
    addExecutorTypeImports(imports, node.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE));
    final Executable executable = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      addExecutorTypeImports(imports, executable.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE));
    }
  }

  private void addExecutorTypeImports(Set<String> imports, ExecutorType type) {
    if (type == ExecutorType.NONE) {
      return;
    }

    if (type == ExecutorType.PLAYER) {
      imports.add(PaperClasses.PLAYER);
    } else if (type == ExecutorType.ENTITY) {
      imports.add(PaperClasses.ENTITY);
    }

    imports.add(Classes.SIMPLE_COMMAND_EXCEPTION_TYPE);
    imports.add(PaperClasses.MESSAGE_COMPONENT_SERIALIZER);
    imports.add(PaperClasses.COMPONENT);
  }
}
