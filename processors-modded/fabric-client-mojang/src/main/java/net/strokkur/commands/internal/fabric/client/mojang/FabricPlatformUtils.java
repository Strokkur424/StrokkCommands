package net.strokkur.commands.internal.fabric.client.mojang;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.fabric.client.mojang.util.FabricClasses;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;

import java.util.List;

final class FabricPlatformUtils implements PlatformUtils {
  @Override
  public void populateNode(final CommandNode node, final AnnotationsHolder element) {
    // TODO: Perhaps op-level handling?
  }

  @Override
  public void populateExecutesNode(final Executable executable, final CommandNode node, final List<SourceParameter> parameters) throws UnknownSenderException {
    // noop
  }

  @Override
  public String getPlatformType() {
    return FabricClasses.FABRIC_CLIENT_COMMAND_SOURCE;
  }
}
