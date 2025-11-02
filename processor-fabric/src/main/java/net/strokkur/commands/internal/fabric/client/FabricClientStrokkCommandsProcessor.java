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
package net.strokkur.commands.internal.fabric.client;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.fabric.FabricStrokkCommandsProcessor;
import net.strokkur.commands.internal.fabric.util.FabricCommandInformation;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.modded.ClientCommand;

public final class FabricClientStrokkCommandsProcessor extends FabricStrokkCommandsProcessor<ClientCommand> {

  @Override
  protected Class<ClientCommand> targetAnnotationClass() {
    return ClientCommand.class;
  }

  @Override
  protected String getCommandName(final ClientCommand annotation) {
    return annotation.value();
  }

  @Override
  protected PlatformUtils getPlatformUtils() {
    return new FabricClientPlatformUtils();
  }

  @Override
  protected CommonCommandTreePrinter<FabricCommandInformation> createPrinter(final CommandNode node, final FabricCommandInformation commandInformation) {
    return new FabricClientCommandTreePrinter(0, null, node, commandInformation, this.processingEnv, getPlatformUtils());
  }

}
