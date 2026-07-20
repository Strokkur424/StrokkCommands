import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.velocity.VelocityPlatformUtils;
import org.jspecify.annotations.NullMarked;

@NullMarked
module net.strokkur.commands.processor.velocity {
  requires net.strokkur.commands.processor.common;
  requires net.strokkur.commands.velocity;

  provides PlatformUtils with VelocityPlatformUtils;
}
