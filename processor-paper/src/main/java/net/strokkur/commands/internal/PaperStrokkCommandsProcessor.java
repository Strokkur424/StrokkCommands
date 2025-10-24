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
package net.strokkur.commands.internal;

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.PaperBrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.PaperTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.parsing.CommandParser;
import net.strokkur.commands.internal.parsing.DefaultExecutesTransform;
import net.strokkur.commands.internal.parsing.ExecutesTransform;
import net.strokkur.commands.internal.parsing.PaperDefaultExecutesTransform;
import net.strokkur.commands.internal.parsing.PaperExecutesTransform;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.printer.PaperCommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.util.PaperCommandInformation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@NullMarked
public final class PaperStrokkCommandsProcessor extends StrokkCommandsProcessor<PaperCommandInformation> {
  private @Nullable PlatformUtils platformUtils = null;

  @Override
  void init() {
    final MessagerWrapper messager = MessagerWrapper.wrap(this.processingEnv.getMessager());
    final BrigadierArgumentConverter converter = new PaperBrigadierArgumentConverter(messager);
    this.platformUtils = new PaperPlatformUtils(messager, converter);
  }

  @Override
  PlatformUtils getPlatformUtils() {
    return Objects.requireNonNull(this.platformUtils);
  }

  @Override
  CommonTreePostProcessor createPostProcessor(final MessagerWrapper messager) {
    return new PaperTreePostProcessor(messager);
  }

  @Override
  CommonCommandTreePrinter<PaperCommandInformation> createPrinter(final CommandNode node, final PaperCommandInformation commandInformation) {
    return new PaperCommandTreePrinter(0, null, node, commandInformation, processingEnv);
  }

  @Override
  ExecutesTransform createExecutesTransform(final CommandParser parser) {
    return new PaperExecutesTransform(parser, Objects.requireNonNull(this.platformUtils));
  }

  @Override
  DefaultExecutesTransform createDefaultExecutesTransform(final CommandParser parser) {
    return new PaperDefaultExecutesTransform(parser, Objects.requireNonNull(this.platformUtils));
  }

  @Override
  PaperCommandInformation getCommandInformation(final SourceClass sourceClass) {
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
