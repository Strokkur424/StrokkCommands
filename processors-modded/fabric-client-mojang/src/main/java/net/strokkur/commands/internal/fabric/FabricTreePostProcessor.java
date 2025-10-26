package net.strokkur.commands.internal.fabric;

import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.MessagerWrapper;

final class FabricTreePostProcessor extends CommonTreePostProcessor {
  public FabricTreePostProcessor(final MessagerWrapper delegateMessager) {
    super(delegateMessager);
  }

  @Override
  public void cleanupPath(final CommandNode root) {
    // noop
  }
}
