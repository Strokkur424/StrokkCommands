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
package net.strokkur.commands.internal.neoforge;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.modded.ModdedCommandTreePrinter;
import net.strokkur.commands.internal.modded.util.ModdedCommandInformation;
import net.strokkur.commands.internal.neoforge.util.NeoForgeClasses;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class NeoForgeCommandTreePrinter extends ModdedCommandTreePrinter {
  private final String registerEvent;

  public NeoForgeCommandTreePrinter(
      final String registerEvent,
      final CommandNode node,
      final ModdedCommandInformation commandInformation,
      final ProcessingEnvironment environment,
      final PlatformUtils utils
  ) {
    super(0, null, node, commandInformation, environment, utils);
    this.registerEvent = registerEvent;
  }

  private String eventClassName() {
    return List.of(registerEvent.split("\\.")).getLast();
  }

  @Override
  protected void printerRegisterJavaDoc() throws IOException {
    //noinspection EscapedSpace
    printBlock("""
            \s* This method should be called inside a {@link %s}.
             * <p>
             * <pre>{@code
             * @SubscribeEvent
             * void registerCommands(final %s event) {
             *   %s.register(event.getDispatcher(), event.getBuildContext());
             * }
             * }</pre>""",
        eventClassName(),
        eventClassName(),
        getBrigadierClassName()
    );
  }

  @Override
  public Set<String> standardImports() {
    final Set<String> out = new TreeSet<>(super.standardImports());
    out.add(this.registerEvent);
    out.add(NeoForgeClasses.COMMANDS);
    out.add(NeoForgeClasses.COMMAND_SOURCE_STACK);
    return Collections.unmodifiableSet(out);
  }
}
