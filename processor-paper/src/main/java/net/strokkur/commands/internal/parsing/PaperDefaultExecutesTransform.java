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
package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutableImpl;
import net.strokkur.commands.internal.intermediate.attributes.PaperAttributeKeys;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class PaperDefaultExecutesTransform extends DefaultExecutesTransform {
  public PaperDefaultExecutesTransform(final CommandParser parser, final PlatformUtils platformUtils) {
    super(parser, platformUtils);
  }

  @Override
  protected void populatePath(final SourceMethod method, final CommandNode node, final List<CommandArgument> args, final List<SourceParameter> parameters) {
    final ExecutorType type = PaperExecutesTransform.getExecutorType(parameters);

    final DefaultExecutable executable = new DefaultExecutableImpl(method, args, DefaultExecutable.Type.getType(parameters.getLast()));
    executable.setAttribute(PaperAttributeKeys.EXECUTOR_TYPE, type);

    node.setAttribute(AttributeKey.DEFAULT_EXECUTABLE, executable);
    node.setAttribute(PaperAttributeKeys.EXECUTOR_TYPE, type);
  }

  @Override
  protected int firstParameterToParse(final List<SourceParameter> parameters) {
    return PaperExecutesTransform.getExecutorType(parameters) == ExecutorType.NONE ? 1 : 2;
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return this.platformUtils;
  }
}
