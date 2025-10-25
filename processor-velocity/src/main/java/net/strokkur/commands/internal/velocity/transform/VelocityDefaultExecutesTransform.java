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
package net.strokkur.commands.internal.velocity.transform;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutableImpl;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.parsing.CommandParser;
import net.strokkur.commands.internal.parsing.DefaultExecutesTransform;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class VelocityDefaultExecutesTransform extends DefaultExecutesTransform {
  public VelocityDefaultExecutesTransform(final CommandParser parser, final PlatformUtils platformUtils) {
    super(parser, platformUtils);
  }

  @Override
  protected void populatePath(
      final SourceMethod method,
      final CommandNode node,
      final List<CommandArgument> args,
      final List<SourceParameter> parameters
  ) throws UnknownSenderException {
    final SenderType type = VelocityExecutesTransform.getSenderType(parameters);

    final DefaultExecutable executable = new DefaultExecutableImpl(method, args, DefaultExecutable.Type.getType(parameters.getLast()));
    executable.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);

    node.setAttribute(AttributeKey.DEFAULT_EXECUTABLE, executable);
    node.setAttribute(VelocityAttributeKeys.SENDER_TYPE, type);
  }
}
