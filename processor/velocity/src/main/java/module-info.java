import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.TreePostProcessor;
import net.strokkur.commands.internal.prototype.PrototypeNodeBuilder;
import net.strokkur.commands.internal.velocity.VelocityBrigadierArgumentConverter;
import net.strokkur.commands.internal.velocity.VelocityPlatformUtils;
import net.strokkur.commands.internal.velocity.VelocityPrototypeNodeBuilder;
import net.strokkur.commands.internal.velocity.VelocityStrokkCommandsProcessor;
import net.strokkur.commands.internal.velocity.VelocityTreePostProcessor;
import org.jspecify.annotations.NullMarked;

import javax.annotation.processing.Processor;

@NullMarked
module net.strokkur.commands.processor.velocity {
  requires net.strokkur.commands.processor.common;
  requires net.strokkur.commands.velocity;
  requires com.google.auto.service;

  provides Processor with VelocityStrokkCommandsProcessor;
  provides PlatformUtils with VelocityPlatformUtils;
  provides TreePostProcessor with VelocityTreePostProcessor;
  provides PrototypeNodeBuilder with VelocityPrototypeNodeBuilder;
  provides BrigadierArgumentConverter with VelocityBrigadierArgumentConverter;
}
