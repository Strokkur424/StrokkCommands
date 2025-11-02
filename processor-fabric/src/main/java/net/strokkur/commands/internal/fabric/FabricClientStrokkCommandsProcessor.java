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
package net.strokkur.commands.internal.fabric;

import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.fabric.util.FabricClasses;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.modded.ModdedStrokkCommandsProcessor;
import net.strokkur.commands.internal.modded.util.ModdedCommandInformation;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.modded.ClientCommand;

public final class FabricClientStrokkCommandsProcessor extends ModdedStrokkCommandsProcessor<ClientCommand> {

  @Override
  protected String getPlatformType() {
    return FabricClasses.FABRIC_CLIENT_COMMAND_SOURCE;
  }

  @Override
  protected Class<ClientCommand> targetAnnotationClass() {
    return ClientCommand.class;
  }

  @Override
  protected String getCommandName(final ClientCommand annotation) {
    return annotation.value();
  }

  @Override
  protected CommonCommandTreePrinter<ModdedCommandInformation> createPrinter(final CommandNode node, final ModdedCommandInformation commandInformation) {
    return new FabricClientCommandTreePrinter(0, null, node, commandInformation, this.processingEnv, getPlatformUtils());
  }

  @Override
  protected BrigadierArgumentConverter getConverter(final MessagerWrapper messager) {
    // Fabric's client commands cannot comprehend complex arguments
    return new BrigadierArgumentConverter(messager);
  }
}
