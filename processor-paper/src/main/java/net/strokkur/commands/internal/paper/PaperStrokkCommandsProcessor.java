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
package net.strokkur.commands.internal.paper;

import net.strokkur.commands.Aliases;
import net.strokkur.commands.Command;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.PaperCommandInformation;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.paper.Description;

import java.util.Optional;

public final class PaperStrokkCommandsProcessor extends StrokkCommandsProcessor<Command, PaperCommandInformation> {

  @Override
  protected Class<Command> targetAnnotationClass() {
    return Command.class;
  }

  @Override
  protected String getCommandName(final Command annotation) {
    return annotation.value();
  }

  @Override
  protected PlatformUtils getPlatformUtils() {
    return new PaperPlatformUtils();
  }

  @Override
  protected BrigadierArgumentConverter getConverter(MessagerWrapper messager) {
    return new PaperBrigadierArgumentConverter(messager);
  }

  @Override
  protected CommonTreePostProcessor createPostProcessor(final MessagerWrapper messager) {
    return new PaperTreePostProcessor(messager);
  }

  @Override
  protected CommonCommandTreePrinter<PaperCommandInformation> createPrinter(final CommandNode node, final PaperCommandInformation commandInformation) {
    return new PaperCommandTreePrinter(0, null, node, commandInformation, processingEnv, getPlatformUtils());
  }

  @Override
  protected PaperCommandInformation getCommandInformation(final SourceClass sourceClass) {
    final Optional<Description> description = sourceClass.getAnnotationOptional(Description.class);
    final Optional<Aliases> aliases = sourceClass.getAnnotationOptional(Aliases.class);

    final SourceConstructor constructor = sourceClass.isRecord() ?
        null :
        sourceClass.getNestedMethods(SourceMethod::isConstructor)
            .stream()
            .findFirst()
            .map(SourceConstructor.class::cast)
            .orElse(null);

    return new PaperCommandInformation(
        constructor,
        sourceClass,
        description.map(Description::value).orElse(null),
        aliases.map(Aliases::value).orElse(null)
    );
  }
}
