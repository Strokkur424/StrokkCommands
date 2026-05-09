package net.strokkur.commands.internal.codegen.builder;

import net.strokkur.commands.internal.codegen.CodeBlock;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeConstructor;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodeParameter;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.Modifiers;
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MethodBuilder {
  private @Nullable CodeClass declaredClass = null;
  private @Nullable String name = null;

  private CodeType returnType = CodeType.VOID;
  private List<CodeParameter> parameters = new ArrayList<>();
  private Set<Modifiers> modifiers = new HashSet<>();
  private @Nullable CodeJavadoc javadoc = null;
  private CodeBlock codeBlock = new CodeBlock(List.of());
  private Set<CodeType.ClassType> throwsExceptions = Set.of();

  MethodBuilder() {
    // package-private ctor
  }

  public MethodBuilder setDeclaringClass(CodeClass declaringClass) {
    this.declaredClass = declaringClass;
    return this;
  }

  public MethodBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public MethodBuilder addParameter(CodeType type, String name) {
    this.parameters.add(CodeParameter.of(type, name));
    return this;
  }

  public MethodBuilder setReturnType(CodeType returnType) {
    this.returnType = returnType;
    return this;
  }

  public MethodBuilder setParameters(List<CodeParameter> parameters) {
    this.parameters = parameters;
    return this;
  }

  public MethodBuilder setModifiers(Set<Modifiers> modifiers) {
    this.modifiers = modifiers;
    return this;
  }

  public MethodBuilder setJavadoc(@Nullable CodeJavadoc javadoc) {
    this.javadoc = javadoc;
    return this;
  }

  public MethodBuilder setCodeBlock(List<CodeStatement> statements) {
    this.codeBlock = new CodeBlock(statements);
    return this;
  }

  public MethodBuilder setThrowsExceptions(Set<CodeType.ClassType> throwsExceptions) {
    this.throwsExceptions = throwsExceptions;
    return this;
  }

  public CodeMethod build() {
    Objects.requireNonNull(this.declaredClass);
    Objects.requireNonNull(this.name);
    return new CodeMethod(
        declaredClass,
        returnType,
        name,
        List.copyOf(parameters),
        Set.copyOf(modifiers),
        javadoc,
        codeBlock,
        Set.copyOf(throwsExceptions)
    );
  }

  public CodeConstructor buildConstructor() {
    Objects.requireNonNull(this.declaredClass);
    return new CodeConstructor(
        declaredClass,
        List.copyOf(parameters),
        Set.copyOf(modifiers),
        javadoc,
        codeBlock,
        Set.copyOf(throwsExceptions)
    );
  }
}
