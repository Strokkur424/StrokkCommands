import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.paper.PaperBrigadierArgumentConverter;
import net.strokkur.commands.internal.paper.PaperPlatformUtils;
import net.strokkur.commands.internal.paper.PaperPrototypeNodeBuilder;
import net.strokkur.commands.internal.paper.PaperStrokkCommandsProcessor;
import net.strokkur.commands.internal.prototype.PrototypeNodeBuilder;
import org.jspecify.annotations.NullMarked;

import javax.annotation.processing.Processor;

@NullMarked
module net.strokkur.commands.processor.paper {
  requires net.strokkur.commands.processor.common;
  requires net.strokkur.commands.paper;
  requires static com.google.auto.service;

  provides Processor with PaperStrokkCommandsProcessor;
  provides PlatformUtils with PaperPlatformUtils;
  provides PrototypeNodeBuilder with PaperPrototypeNodeBuilder;
  provides BrigadierArgumentConverter with PaperBrigadierArgumentConverter;
}
