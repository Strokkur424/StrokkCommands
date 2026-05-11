/*
 * StrokkCommands - A super simple annotation based zero-shade Paper command API library.
 * Copyright (C) 2025 Strokkur24
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <https://www.gnu.org/licenses/>.
 */
package net.strokkur.commands.internal.codegen.builder;

import net.strokkur.commands.internal.codegen.CodeBlock;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeConstructor;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodeParameter;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.Modifiers;
import net.strokkur.commands.internal.codegen.as.AsCodeType;
import net.strokkur.commands.internal.codegen.as.AsStatement;
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import net.strokkur.commands.internal.util.ConvertableTo;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class MethodBuilder implements ConvertableTo<CodeMethod> {
  private @Nullable CodeClass declaredClass = null;
  private final String name;

  private CodeType returnType = CodeType.VOID;
  private final List<CodeParameter> parameters = new ArrayList<>();
  private Set<Modifiers> modifiers = new HashSet<>();
  private @Nullable CodeJavadoc javadoc = null;
  private List<CodeStatement> methodStatements = new ArrayList<>();
  private List<CodeType.ClassType> throwsExceptions = List.of();

  MethodBuilder(String name) {
    this.name = name;
  }

  public MethodBuilder setDeclaringClass(CodeClass declaringClass) {
    this.declaredClass = declaringClass;
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

  public MethodBuilder setModifiers(Modifiers... modifiers) {
    return setModifiers(Set.of(modifiers));
  }

  public MethodBuilder addModifiers(Modifiers... modifiers) {
    this.modifiers.addAll(Set.of(modifiers));
    return this;
  }

  public MethodBuilder setModifiers(Set<Modifiers> modifiers) {
    this.modifiers = new HashSet<>(modifiers);
    return this;
  }

  public MethodBuilder setJavadoc(@Nullable CodeJavadoc javadoc) {
    this.javadoc = javadoc;
    return this;
  }

  public MethodBuilder addMethodStatements(AsStatement... statements) {
    this.methodStatements.addAll(Arrays.stream(statements)
        .map(AsStatement::getAsStatement)
        .toList());
    return this;
  }

  public MethodBuilder setMethodStatements(AsStatement... statements) {
    return setMethodStatements(List.of(statements));
  }

  public MethodBuilder setMethodStatements(List<? extends AsStatement> statements) {
    this.methodStatements = new ArrayList<>(statements.stream()
        .map(AsStatement::getAsStatement)
        .toList());
    return this;
  }

  public MethodBuilder setThrowsExceptions(AsCodeType<CodeType.ClassType>... throwsExceptions) {
    this.throwsExceptions = Arrays.stream(throwsExceptions)
        .map(AsCodeType::getAsCodeType)
        .toList();
    return this;
  }

  public MethodBuilder setThrowsExceptions(CodeType.ClassType... throwsExceptions) {
    this.throwsExceptions = List.of(throwsExceptions);
    return this;
  }

  public CodeMethod build() {
    Objects.requireNonNull(this.name);
    return new CodeMethod(
        Optional.ofNullable(declaredClass).orElse(CodeClass.simple("no.class.provided.in.MethodBuilder")),
        returnType,
        name,
        List.copyOf(parameters),
        Set.copyOf(modifiers),
        javadoc,
        new CodeBlock(List.copyOf(methodStatements)),
        List.copyOf(throwsExceptions)
    );
  }

  public CodeConstructor buildConstructor() {
    Objects.requireNonNull(this.declaredClass);
    return new CodeConstructor(
        declaredClass,
        List.copyOf(parameters),
        Set.copyOf(modifiers),
        javadoc,
        new CodeBlock(List.copyOf(methodStatements)),
        List.copyOf(throwsExceptions)
    );
  }

  @Override
  public CodeMethod convert() {
    return build();
  }
}
