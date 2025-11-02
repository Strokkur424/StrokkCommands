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

import net.strokkur.commands.Aliases;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.fabric.client.FabricClientStrokkCommandsProcessor;
import net.strokkur.commands.internal.fabric.server.FabricServerStrokkCommandsProcessor;
import net.strokkur.commands.internal.fabric.util.FabricCommandInformation;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.util.MessagerWrapper;

import java.lang.annotation.Annotation;
import java.util.Optional;

public abstract sealed class FabricStrokkCommandsProcessor<A extends Annotation>
    extends StrokkCommandsProcessor<A, FabricCommandInformation>
    permits FabricServerStrokkCommandsProcessor, FabricClientStrokkCommandsProcessor {
  @Override
  protected CommonTreePostProcessor createPostProcessor(final MessagerWrapper messager) {
    return new FabricTreePostProcessor(messager);
  }

  @Override
  protected FabricCommandInformation getCommandInformation(final SourceClass sourceClass) {
    final Optional<Aliases> aliases = sourceClass.getAnnotationOptional(Aliases.class);

    final SourceConstructor constructor = sourceClass.isRecord() ?
        null :
        sourceClass.getNestedMethods(SourceMethod::isConstructor)
            .stream()
            .findFirst()
            .map(SourceConstructor.class::cast)
            .orElse(null);

    return new FabricCommandInformation(
        constructor,
        sourceClass,
        aliases.map(Aliases::value).orElse(new String[0])
    );
  }

  @Override
  protected BrigadierArgumentConverter getConverter(MessagerWrapper messager) {
    return new FabricArgumentConverter(messager);
  }
}
