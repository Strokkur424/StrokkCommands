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

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.parsing.CommandParser;
import net.strokkur.commands.internal.parsing.DefaultExecutesTransform;
import net.strokkur.commands.internal.parsing.ExecutesTransform;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.velocity.transform.VelocityDefaultExecutesTransform;
import net.strokkur.commands.internal.velocity.transform.VelocityExecutesTransform;
import net.strokkur.commands.internal.velocity.util.VelocityCommandInformation;
import net.strokkur.commands.velocity.Aliases;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class VelocityStrokkCommandsProcessor extends StrokkCommandsProcessor<VelocityCommandInformation> {
  private @Nullable PlatformUtils platformUtils;

  @Override
  protected void init() {
    final MessagerWrapper messager = MessagerWrapper.wrap(this.processingEnv.getMessager());
    final BrigadierArgumentConverter converter = new VelocityBrigadierArgumentConverter(messager);
    this.platformUtils = new VelocityPlatformUtils(messager, converter);
  }

  @Override
  protected PlatformUtils getPlatformUtils() {
    return Objects.requireNonNull(this.platformUtils);
  }

  @Override
  protected CommonTreePostProcessor createPostProcessor(final MessagerWrapper messager) {
    return new VelocityTreePostProcessor(messager);
  }

  @Override
  protected CommonCommandTreePrinter<VelocityCommandInformation> createPrinter(final CommandNode node, final VelocityCommandInformation commandInformation) {
    return new VelocityCommandTreePrinter(0, null, node, commandInformation, this.processingEnv);
  }

  @Override
  protected ExecutesTransform createExecutesTransform(final CommandParser parser) {
    return new VelocityExecutesTransform(parser, getPlatformUtils());
  }

  @Override
  protected DefaultExecutesTransform createDefaultExecutesTransform(final CommandParser parser) {
    return new VelocityDefaultExecutesTransform(parser, getPlatformUtils());
  }

  @Override
  protected VelocityCommandInformation getCommandInformation(final SourceClass sourceClass) {
    final Optional<Aliases> aliases = sourceClass.getAnnotationOptional(Aliases.class);

    final SourceConstructor constructor = sourceClass.isRecord() ?
        null :
        sourceClass.getNestedMethods(SourceMethod::isConstructor)
            .stream()
            .findFirst()
            .map(SourceConstructor.class::cast)
            .orElse(null);

    return new VelocityCommandInformation(
        constructor,
        sourceClass,
        aliases.map(Aliases::value).orElse(null)
    );
  }
}
