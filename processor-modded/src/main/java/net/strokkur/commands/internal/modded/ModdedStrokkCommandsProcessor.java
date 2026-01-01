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
package net.strokkur.commands.internal.modded;

import net.strokkur.commands.Aliases;
import net.strokkur.commands.ExecutorWrapper;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.modded.util.ModdedClasses;
import net.strokkur.commands.internal.modded.util.ModdedCommandInformation;
import net.strokkur.commands.internal.util.MessagerWrapper;

import java.lang.annotation.Annotation;
import java.util.Optional;

public abstract class ModdedStrokkCommandsProcessor<A extends Annotation> extends StrokkCommandsProcessor<A, ModdedCommandInformation>{

  protected String getPlatformType() {
    return ModdedClasses.COMMAND_SOURCE_STACK;
  }

  @Override
  protected final CommonTreePostProcessor createPostProcessor(final MessagerWrapper messager) {
    return new ModdedTreePostProcessor(messager);
  }

  @Override
  protected final PlatformUtils getPlatformUtils() {
    return new ModdedPlatformUtils(getPlatformType());
  }

  @Override
  protected final ModdedCommandInformation getCommandInformation(final SourceClass sourceClass) {
    final Optional<Aliases> aliases = sourceClass.getAnnotationOptional(Aliases.class);

    final SourceConstructor constructor = sourceClass.isRecord() ?
        null :
        sourceClass.getNestedMethods(SourceMethod::isConstructor)
            .stream()
            .findFirst()
            .map(SourceConstructor.class::cast)
            .orElse(null);

    final SourceMethod executorWrapper = sourceClass.getNestedMethods(m -> m.hasAnnotation(ExecutorWrapper.class))
        .stream()
        .findFirst()
        .orElse(null);

    return new ModdedCommandInformation(
        constructor,
        sourceClass,
        aliases.map(Aliases::value).orElse(new String[0]),
        executorWrapper
    );
  }

  @Override
  protected BrigadierArgumentConverter getConverter(MessagerWrapper messager) {
    return new ModdedArgumentConverter(messager);
  }
}
