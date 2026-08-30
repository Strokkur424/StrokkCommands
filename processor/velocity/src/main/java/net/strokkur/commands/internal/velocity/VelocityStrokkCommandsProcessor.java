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

import com.google.auto.service.AutoService;
import net.strokkur.commands.Aliases;
import net.strokkur.commands.Command;
import net.strokkur.commands.UseInjection;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonClassBuilder;
import net.strokkur.commands.internal.velocity.util.VelocityCommandInformation;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceConstructor;

import javax.annotation.processing.Processor;
import java.util.Optional;

@AutoService(Processor.class)
public final class VelocityStrokkCommandsProcessor extends StrokkCommandsProcessor<Command, VelocityCommandInformation> {

  @Override
  protected Class<Command> targetAnnotationClass() {
    return Command.class;
  }

  @Override
  protected CommonClassBuilder<VelocityCommandInformation> createBuilder(CommandNode node, VelocityCommandInformation commandInformation) {
    return new VelocityClassBuilder(node, commandInformation);
  }

  @Override
  protected VelocityCommandInformation getCommandInformation(SourceClassLike classLike) {
    final Optional<Aliases> aliases = classLike.findAnnotationValue(Aliases.class);

    final SourceConstructor constructor = classLike instanceof SourceClass sourceClass
      ? sourceClass.constructors().stream().findFirst().orElse(null)
      : null;

    return new VelocityCommandInformation(
      classLike.getAnnotationValue(Command.class).value().split(" ")[0],
      constructor,
      classLike,
      aliases.map(Aliases::value).orElse(null),
      classLike.hasAnnotationInherited(UseInjection.class)
    );
  }
}
