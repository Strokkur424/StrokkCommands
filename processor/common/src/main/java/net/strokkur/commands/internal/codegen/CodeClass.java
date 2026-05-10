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
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public record CodeClass(
    CodePackage codePackage, @Nullable CodeClass parentClass, String name, Set<Modifiers> modifiers,
    List<CodeAnnotation> annotations, List<CodeMethod> methods, List<CodeField> fields,
    @Nullable CodeJavadoc javadoc,
    List<CodeType.GenericType> typeParameters
) implements CodeVisitable, ConvertableTo.Self<CodeClass> {
  public static CodeClass OBJECT = CodeClass.simple("java.lang.Object");
  public static CodeClass LIST = CodeClass.simple("java.util.List");
  public static CodeClass STRING = CodeClass.simple("java.lang.String");

  private CodeClass(
      CodePackage codePackage,
      @Nullable CodeClass parentClass,
      String name
  ) {
    this(codePackage, parentClass, name, Set.of(), List.of(), new ArrayList<>(), new ArrayList<>(), null, List.of());
  }

  public static CodeClass simple(String string) {
    final String[] split = string.split("\\.");
    return new CodeClass(
        new CodePackage(Arrays.copyOf(split, split.length - 1)),
        null,
        split[split.length - 1]
    );
  }

  @Override
  public <R> R accept(CodeVisitor<R> visitor) {
    return visitor.visitClass(this);
  }

  public String fullyQualifiedName() {
    return codePackage().path() + "." + name();
  }

  @Override
  @UnmodifiableView
  public Set<Modifiers> modifiers() {
    return Collections.unmodifiableSet(modifiers);
  }

  @Override
  @UnmodifiableView
  public List<CodeAnnotation> annotations() {
    return Collections.unmodifiableList(annotations);
  }

  @Override
  @UnmodifiableView
  public List<CodeMethod> methods() {
    return Collections.unmodifiableList(methods);
  }

  @Override
  @UnmodifiableView
  public List<CodeField> fields() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  @UnmodifiableView
  public List<CodeType.GenericType> typeParameters() {
    return Collections.unmodifiableList(typeParameters);
  }
}
