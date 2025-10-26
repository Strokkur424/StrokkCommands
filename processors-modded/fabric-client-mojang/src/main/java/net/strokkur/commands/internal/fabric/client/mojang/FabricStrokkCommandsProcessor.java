package net.strokkur.commands.internal.fabric.client.mojang;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.fabric.client.mojang.util.FabricCommandInformation;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.modded.Aliases;

import java.util.Optional;

public class FabricStrokkCommandsProcessor extends StrokkCommandsProcessor<FabricCommandInformation> {
  @Override
  protected PlatformUtils getPlatformUtils() {
    return new FabricPlatformUtils();
  }

  @Override
  protected CommonTreePostProcessor createPostProcessor(final MessagerWrapper messager) {
    return new FabricTreePostProcessor(messager);
  }

  @Override
  protected CommonCommandTreePrinter<FabricCommandInformation> createPrinter(final CommandNode node, final FabricCommandInformation commandInformation) {
    return new FabricCommandTreePrinter(0, null, node, commandInformation, this.processingEnv, getPlatformUtils());
  }

  @Override
  protected BrigadierArgumentConverter getConverter() {
    return new FabricArgumentConverter(MessagerWrapper.wrap(this.processingEnv.getMessager()));
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
