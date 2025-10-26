package net.strokkur.commands.internal.fabric;

import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.fabric.client.FabricClientStrokkCommandsProcessor;
import net.strokkur.commands.internal.fabric.server.FabricServerStrokkCommandsProcessor;
import net.strokkur.commands.internal.fabric.util.FabricCommandInformation;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.modded.Aliases;

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
}
