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
import net.strokkur.commands.internal.util.ConvertableTo;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ClassBuilder implements ConvertableTo<CodeClass> {
  private final CodePackage codePackage;
  private @Nullable CodeClass parentClass = null;
  private final String name;
  private Set<Modifiers> modifiers = new HashSet<>();
  private List<CodeAnnotation> annotations = new ArrayList<>();
  private final List<CodeMethod> methods = new ArrayList<>();
  private final List<CodeField> fields = new ArrayList<>();
  private @Nullable CodeJavadoc javadoc = null;
  private List<CodeType.GenericType> typeParameters = new ArrayList<>();

  ClassBuilder(String name, CodePackage codePackage) {
    this.codePackage = codePackage;
    this.name = name;
  }

  public ClassBuilder setParentClass(@Nullable ConvertableTo<CodeClass> parentClass) {
    this.parentClass = Optional.ofNullable(parentClass).map(ConvertableTo::convert).orElse(null);
    return this;
  }

  public ClassBuilder setModifiers(Modifiers... modifiers) {
    return setModifiers(Set.of(modifiers));
  }

  public ClassBuilder setModifiers(Set<Modifiers> modifiers) {
    this.modifiers = modifiers;
    return this;
  }

  public ClassBuilder setAnnotations(List<CodeAnnotation> annotations) {
    this.annotations = new ArrayList<>(annotations);
    return this;
  }

  public ClassBuilder addAnnotations(CodeAnnotation... annotations) {
    this.annotations.addAll(List.of(annotations));
    return this;
  }

  public ClassBuilder setJavadoc(@Nullable CodeJavadoc javadoc) {
    this.javadoc = javadoc;
    return this;
  }

  public ClassBuilder setTypeParameters(List<CodeType.GenericType> typeParameters) {
    this.typeParameters = typeParameters;
    return this;
  }

  public ClassBuilder addMethod(ConvertableTo<CodeMethod> method) {
    this.methods.add(method.convert());
    return this;
  }

  public ClassBuilder addField(ConvertableTo<CodeField> field) {
    this.fields.add(field.convert());
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

  @Override
  public CodeClass convert() {
    return build();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof final ClassBuilder that)) {
      return false;
    }
    return Objects.equals(codePackage, that.codePackage) && Objects.equals(parentClass, that.parentClass) && Objects.equals(name, that.name) && Objects.equals(modifiers, that.modifiers) && Objects.equals(annotations, that.annotations) && Objects.equals(methods, that.methods) && Objects.equals(fields, that.fields) && Objects.equals(javadoc, that.javadoc) && Objects.equals(typeParameters, that.typeParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codePackage, parentClass, name, modifiers, annotations, methods, fields, javadoc, typeParameters);
  }
}
