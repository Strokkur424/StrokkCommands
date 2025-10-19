package net.strokkur.commands.internal.intermediate.attributes;

import net.strokkur.commands.internal.intermediate.ExecutorType;

import javax.lang.model.element.ExecutableElement;

public interface Executable extends Parameterizable {
  ExecutorType executorType();
  ExecutableElement executesMethod();
}
