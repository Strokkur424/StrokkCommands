package net.strokkur.commands.internal.fabric.server;

import net.strokkur.commands.internal.fabric.FabricPlatformUtils;
import net.strokkur.commands.internal.fabric.util.FabricClasses;

public final class FabricServerPlatformUtils extends FabricPlatformUtils {

  @Override
  public String getPlatformType() {
    return FabricClasses.COMMAND_SOURCE_STACK;
  }
}
