package net.strokkur.commands.internal.codegen.as;

import net.strokkur.commands.internal.codegen.CodeType;

public interface AsCodeType<S extends CodeType> {
  S getAsCodeType();
}
