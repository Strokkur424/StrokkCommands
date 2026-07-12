package net.strokkur.commands.internal.exceptions;

import net.strokkur.jap.source.classmodel.SourceClassLike;

public class IllegalCommandClassTypeException extends RuntimeException {
  private final SourceClassLike classType;

  public IllegalCommandClassTypeException(SourceClassLike classType) {
    this.classType = classType;
  }

  public SourceClassLike classType() {
    return classType;
  }
}
