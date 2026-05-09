package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class CodeConstructor extends CodeMethod {
  public CodeConstructor(
      CodeClass declaredClass,
      List<CodeParameter> parameters,
      Set<Modifiers> modifiers,
      @Nullable CodeJavadoc javadoc,
      CodeBlock codeBlock,
      Set<CodeType.ClassType> throwsExceptions
  ) {
    super(declaredClass, CodeType.ofClass(declaredClass), declaredClass.name(), parameters, modifiers, javadoc, codeBlock, throwsExceptions);
  }
}
