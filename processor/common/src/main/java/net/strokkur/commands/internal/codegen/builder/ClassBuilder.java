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

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeField;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.Modifiers;
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassBuilder {
  private final CodePackage codePackage;
  private @Nullable CodeClass parentClass = null;
  private final String name;
  private Set<Modifiers> modifiers = new HashSet<>();
  private List<CodeAnnotation> annotations = new ArrayList<>();
  private final List<CodeMethod> methods = new ArrayList<>();
  private final List<CodeField> fields = new ArrayList<>();
  private @Nullable CodeJavadoc javadoc = null;
  private List<CodeType> typeParameters = new ArrayList<>();

  ClassBuilder(String name, CodePackage codePackage) {
    this.codePackage = codePackage;
    this.name = name;
  }

  public ClassBuilder setParentClass(@Nullable CodeClass parentClass) {
    this.parentClass = parentClass;
    return this;
  }

  public ClassBuilder setModifiers(Set<Modifiers> modifiers) {
    this.modifiers = modifiers;
    return this;
  }

  public ClassBuilder setAnnotations(List<CodeAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  public ClassBuilder setJavadoc(@Nullable CodeJavadoc javadoc) {
    this.javadoc = javadoc;
    return this;
  }

  public ClassBuilder setTypeParameters(List<CodeType> typeParameters) {
    this.typeParameters = typeParameters;
    return this;
  }

  public ClassBuilder addMethod(CodeMethod method) {
    this.methods.add(method);
    return this;
  }

  public ClassBuilder addMethod(MethodBuilder methodBuilder) {
    this.methods.add(methodBuilder.build());
    return this;
  }

  public ClassBuilder addField(CodeField field) {
    this.fields.add(field);
    return this;
  }

  public ClassBuilder addField(FieldBuilder fieldBuilder) {
    this.fields.add(fieldBuilder.build());
    return this;
  }

  public CodeClass build() {
    return new CodeClass(
        codePackage,
        parentClass,
        name,
        new HashSet<>(modifiers),
        new ArrayList<>(annotations),
        new ArrayList<>(methods),
        new ArrayList<>(fields),
        javadoc,
        typeParameters
    );
  }
}
