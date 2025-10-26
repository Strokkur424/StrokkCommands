package net.strokkur.commands.internal.fabric.client;

import net.strokkur.commands.internal.fabric.FabricPlatformUtils;
import net.strokkur.commands.internal.fabric.util.FabricClasses;

public final class FabricClientPlatformUtils extends FabricPlatformUtils {

  @Override
  public String getPlatformType() {
    return FabricClasses.FABRIC_CLIENT_COMMAND_SOURCE;
  }
}
