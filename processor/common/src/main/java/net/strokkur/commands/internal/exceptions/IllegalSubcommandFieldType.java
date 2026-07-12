package net.strokkur.commands.internal.exceptions;

import net.strokkur.jap.source.type.SourceType;

public class IllegalSubcommandFieldType extends RuntimeException {
  private final SourceType type;

  public IllegalSubcommandFieldType(SourceType type) {
    this.type = type;
  }

  public SourceType type() {
    return type;
  }
}
