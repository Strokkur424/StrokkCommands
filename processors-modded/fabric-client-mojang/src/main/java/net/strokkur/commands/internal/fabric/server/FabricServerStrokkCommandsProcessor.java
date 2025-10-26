package net.strokkur.commands.internal.fabric.server;

import net.strokkur.commands.Command;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.fabric.FabricStrokkCommandsProcessor;
import net.strokkur.commands.internal.fabric.util.FabricCommandInformation;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;

public final class FabricServerStrokkCommandsProcessor extends FabricStrokkCommandsProcessor<Command> {

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
    return new FabricServerPlatformUtils();
  }

  @Override
  protected CommonCommandTreePrinter<FabricCommandInformation> createPrinter(final CommandNode node, final FabricCommandInformation commandInformation) {
    return new FabricServerCommandTreePrinter(0, null, node, commandInformation, this.processingEnv, getPlatformUtils());
  }

  @Override
  protected BrigadierArgumentConverter getConverter() {
    return new FabricServerArgumentConverter(MessagerWrapper.wrap(this.processingEnv.getMessager()));
  }
}
