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

import net.strokkur.commands.Aliases;
import net.strokkur.commands.Command;
import net.strokkur.commands.UseInjection;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonClassBuilder;
import net.strokkur.commands.internal.velocity.util.VelocityCommandInformation;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceConstructor;

import java.util.Optional;

public final class VelocityStrokkCommandsProcessor extends StrokkCommandsProcessor<Command, VelocityCommandInformation> {

  @Override
  protected Class<Command> targetAnnotationClass() {
    return Command.class;
  }

  @Override
  protected PlatformUtils getPlatformUtils() {
    return new VelocityPlatformUtils();
  }

  @Override
  protected CommonTreePostProcessor createPostProcessor() {
    return new VelocityTreePostProcessor(messager());
  }

  @Override
  protected CommonClassBuilder<VelocityCommandInformation> createBuilder(CommandNode node, VelocityCommandInformation commandInformation) {
    return new VelocityClassBuilder(node, commandInformation);
  }

  @Override
  protected BrigadierArgumentConverter getConverter() {
    return new VelocityBrigadierArgumentConverter(messager());
  }

  @Override
  protected VelocityCommandInformation getCommandInformation(SourceClassLike classLike) {
    final Optional<Aliases> aliases = classLike.firstAnnotationByTypeOptional(Aliases.class)
      .map(anno -> anno.value(Aliases.class));

    final SourceConstructor constructor = classLike instanceof SourceClass sourceClass
      ? sourceClass.constructors().stream().findFirst().orElse(null)
      : null;

    return new VelocityCommandInformation(
      classLike.firstAnnotationByType(Command.class).value(Command.class).value(),
      constructor,
      classLike,
      aliases.map(Aliases::value).orElse(null),
      classLike.hasAnnotationInherited(UseInjection.class)
    );
  }
}
