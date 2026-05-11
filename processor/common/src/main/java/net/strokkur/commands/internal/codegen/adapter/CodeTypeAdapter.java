package net.strokkur.commands.internal.codegen.adapter;

import net.strokkur.commands.internal.abstraction.SourceType;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeType;

import java.util.List;

public final class CodeTypeAdapter {

  public static CodeType.ClassType from(SourceType sourceClass) {
    return CodeType.ofClass(CodeClass.nested(
        CodePackage.of(sourceClass.getPackageName()),
        List.of(sourceClass.getSourceName().split("\\."))
    ));
  }

  private CodeTypeAdapter() {
  }
}
