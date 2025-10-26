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
import net.strokkur.commands.internal.fabric.FabricCommandTreePrinter;
import net.strokkur.commands.internal.fabric.util.FabricClasses;
import net.strokkur.commands.internal.fabric.util.FabricCommandInformation;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public final class FabricClientCommandTreePrinter extends FabricCommandTreePrinter {
  public FabricClientCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final FabricCommandInformation commandInformation,
      final ProcessingEnvironment environment,
      final PlatformUtils utils
  ) {
    super(indent, writer, node, commandInformation, environment, utils);
  }

  @Override
  protected String getSourceName() {
    return "FabricClientCommandSource";
  }

  @Override
  protected String modInitializerJd() {
    return "ClientModInitializer#onInitializeClient()";
  }

  @Override
  protected String registrationCallbackClassName() {
    return "ClientCommandRegistrationCallback";
  }

  @Override
  protected String callbackEventLambdaParams() {
    return "(dispatcher, registryAccess)";
  }

  @Override
  public Set<String> standardImports() {
    final Set<String> out = new TreeSet<>(super.standardImports());
    out.addAll(Set.of(
        FabricClasses.CLIENT_COMMAND_MANAGER,
        FabricClasses.CLIENT_COMMAND_REGISTRATION_CALLBACK,
        FabricClasses.CLIENT_MOD_INITIALIZER,
        FabricClasses.FABRIC_CLIENT_COMMAND_SOURCE
    ));
    return Collections.unmodifiableSet(out);
  }

  @Override
  public String getLiteralMethodString() {
    return "ClientCommandManager.literal";
  }

  @Override
  public String getArgumentMethodString() {
    return "ClientCommandManager.argument";
  }
}
