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
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.fabric.client.FabricClientPlatformUtils;
import net.strokkur.commands.internal.fabric.server.FabricServerPlatformUtils;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;

import java.util.List;

public sealed abstract class FabricPlatformUtils implements PlatformUtils permits FabricClientPlatformUtils, FabricServerPlatformUtils {
  @Override
  public void populateNode(final CommandNode node, final AnnotationsHolder element) {
    // TODO: Perhaps op-level handling?
  }

  @Override
  public void populateExecutesNode(final Executable executable, final CommandNode node, final List<SourceParameter> parameters)
      throws UnknownSenderException {
    // noop
  }
}
