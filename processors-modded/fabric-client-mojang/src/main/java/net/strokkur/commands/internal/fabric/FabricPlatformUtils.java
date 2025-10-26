package net.strokkur.commands.internal.fabric;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.fabric.client.FabricClientPlatformUtils;
import net.strokkur.commands.internal.fabric.server.FabricServerPlatformUtils;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;

import java.util.List;

public sealed abstract class FabricPlatformUtils implements PlatformUtils permits FabricClientPlatformUtils, FabricServerPlatformUtils {
  @Override
  public void populateNode(final CommandNode node, final AnnotationsHolder element) {
    // TODO: Perhaps op-level handling?
  }

  @Override
  public void populateExecutesNode(final Executable executable, final CommandNode node, final List<SourceParameter> parameters)
      throws UnknownSenderException {
    // noop
  }
}
