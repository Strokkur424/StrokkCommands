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

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.modded.ModdedCommandTreePrinter;
import net.strokkur.commands.internal.modded.util.ModdedCommandInformation;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;

public abstract sealed class FabricCommonCommandTreePrinter extends ModdedCommandTreePrinter permits FabricCommandTreePrinter, FabricClientCommandTreePrinter {
  public FabricCommonCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final ModdedCommandInformation commandInformation,
      final ProcessingEnvironment environment,
      final PlatformUtils utils
  ) {
    super(indent, writer, node, commandInformation, environment, utils);
  }

  protected abstract String modInitializerJd();

  protected abstract String callbackEventLambdaParams();

  protected abstract String registrationCallbackClassName();

  protected final void printerRegisterJavaDoc() throws IOException {
    //noinspection EscapedSpace
    printBlock("""
            \s* This method should be called in your main class' {@link %s} method
             * inside of a {@link %s} event. You can find some information on commands
             * in the <a href="https://docs.fabricmc.net/develop/commands/basics">Fabric Documentation</a>.
             * <p>
             * <pre>{@code
             * %s.EVENT.register(%s -> {
             *   %s.register(dispatcher, registryAccess);
             * });
             * }</pre>""",
        modInitializerJd(),
        registrationCallbackClassName(),
        registrationCallbackClassName(),
        callbackEventLambdaParams(),
        getBrigadierClassName()
    );
  }
}
