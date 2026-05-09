package net.strokkur.commands.internal.codegen.builder;

import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeType;

import java.util.Arrays;

public class Builders {
  public static MethodBuilder method() {
    return new MethodBuilder();
  }

  public static MethodBuilder method(CodeClass declaringClass, String name) {
    return new MethodBuilder()
        .setDeclaringClass(declaringClass)
        .setName(name);
  }

  public static FieldBuilder field(String name, CodeType type) {
    return new FieldBuilder()
        .setName(name)
        .setType(type);
  }

  public static ClassBuilder classBuilder(String name, CodePackage codePackage) {
    return new ClassBuilder(name, codePackage);
  }

  public static ClassBuilder classBuilder(String fqn) {
    final String[] split = fqn.split("\\.");
    return new ClassBuilder(split[split.length - 1], new CodePackage(Arrays.copyOf(split, split.length - 1)));
  }
}
