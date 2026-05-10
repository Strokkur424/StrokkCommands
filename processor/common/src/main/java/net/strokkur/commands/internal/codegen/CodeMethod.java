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
package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import net.strokkur.commands.internal.util.ConvertableTo;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class CodeMethod implements CodeVisitable, ConvertableTo.Self<CodeMethod> {
  private final CodeClass declaredClass;
  private final CodeType returnType;
  private final String name;
  private final List<CodeParameter> parameters;
  private final Set<Modifiers> modifiers;
  private final @Nullable CodeJavadoc javadoc;
  private final CodeBlock codeBlock;
  private final List<CodeType.ClassType> throwsExceptions;

  public CodeMethod(
      CodeClass declaredClass,
      CodeType returnType,
      String name,
      List<CodeParameter> parameters,
      Set<Modifiers> modifiers,
      @Nullable CodeJavadoc javadoc,
      CodeBlock codeBlock,
      List<CodeType.ClassType> throwsExceptions
  ) {
    this.declaredClass = declaredClass;
    this.returnType = returnType;
    this.name = name;
    this.parameters = parameters;
    this.modifiers = modifiers;
    this.javadoc = javadoc;
    this.codeBlock = codeBlock;
    this.throwsExceptions = throwsExceptions;
  }

  @Override
  public <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitMethod(this);
  }

  /// Example: `methodName`
  public String name() {
    return name;
  }

  public CodeClass declaredClass() {
    return declaredClass;
  }

  public CodeType returnType() {
    return returnType;
  }

  public List<CodeParameter> parameters() {
    return parameters;
  }

  public Set<Modifiers> modifiers() {
    return modifiers;
  }

  public @Nullable CodeJavadoc javadoc() {
    return javadoc;
  }

  public CodeBlock codeBlock() {
    return codeBlock;
  }

  public List<CodeType.ClassType> throwsExceptions() {
    return throwsExceptions;
  }
}
