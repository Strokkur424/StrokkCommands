package net.strokkur.commands.internal.codegen;

import java.util.Locale;

public enum Modifiers {
  // Visibility
  PUBLIC(0),
  PRIVATE(1),

  // OOP
  ABSTRACT(10),
  DEFAULT(10),

  // Misc.
  FINAL(5),
  STATIC(4);
  private final int priority;

  Modifiers(int priority) {
    this.priority = priority;
  }

  public int priority() {
    return priority;
  }

  @Override
  public String toString() {
    return this.name().toLowerCase(Locale.ROOT);
  }
}
