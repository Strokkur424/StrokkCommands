package net.strokkur.commands.internal.fabric.client;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.fabric.FabricStrokkCommandsProcessor;
import net.strokkur.commands.internal.fabric.util.FabricCommandInformation;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.modded.ClientCommand;

public final class FabricClientStrokkCommandsProcessor extends FabricStrokkCommandsProcessor<ClientCommand> {

  @Override
  protected Class<ClientCommand> targetAnnotationClass() {
    return ClientCommand.class;
  }

  @Override
  protected String getCommandName(final ClientCommand annotation) {
    return annotation.value();
  }

  @Override
  protected PlatformUtils getPlatformUtils() {
    return new FabricClientPlatformUtils();
  }

  @Override
  protected CommonCommandTreePrinter<FabricCommandInformation> createPrinter(final CommandNode node, final FabricCommandInformation commandInformation) {
    return new FabricClientCommandTreePrinter(0, null, node, commandInformation, this.processingEnv, getPlatformUtils());
  }

  @Override
  protected BrigadierArgumentConverter getConverter() {
    return new FabricClientArgumentConverter(MessagerWrapper.wrap(this.processingEnv.getMessager()));
  }
}
