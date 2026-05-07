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
package net.strokkur.commands.internal.abstraction.impl;

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceField;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceTypeAnnotation;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class SourceClassImpl implements SourceClass, ElementGettable<TypeElement> {
  protected final ProcessingEnvironment environment;
  protected final DeclaredType type;
  protected final TypeElement element;

  public SourceClassImpl(final ProcessingEnvironment environment, final DeclaredType type) {
    this.environment = environment;
    this.type = type;
    this.element = (TypeElement) this.type.asElement();
  }

  @Override
  public boolean isTopLevel() {
    return element.getNestingKind() == NestingKind.TOP_LEVEL;
  }

  @Override
  public TypeElement getElement() {
    return this.element;
  }

  @Override
  public SourceClass getOuterClass() throws NoSuchElementException {
    final Element enclosing = element.getEnclosingElement();
    if (enclosing instanceof TypeElement outer) {
      return new SourceClassImpl(this.environment, (DeclaredType) outer.asType());
    }
    throw new NoSuchElementException("Could not find an enclosing class. Found element of kind: " + enclosing.getKind());
  }

  @Override
  public List<SourceTypeAnnotation> getTypeAnnotations() {
    final List<SourceTypeAnnotation> out = new ArrayList<>(this.element.getTypeParameters().size());
    for (final TypeParameterElement typeParameter : this.element.getTypeParameters()) {
      out.add(new SourceTypeAnnotationImpl(this.environment, typeParameter));
    }
    return Collections.unmodifiableList(out);
  }

  @Override
  public List<SourceClass> getImplementedInterfaces() {
    final List<SourceClass> out = new LinkedList<>();
    for (final TypeMirror anInterface : this.element.getInterfaces()) {
      out.add(new SourceClassImpl(this.environment, (DeclaredType) anInterface));
    }
    return Collections.unmodifiableList(out);
  }

  @Override
  public List<SourceMethod> getNestedMethods(final Predicate<SourceMethod> predicate) {
    final List<SourceMethod> out = new LinkedList<>();
    for (final Element element : this.element.getEnclosedElements()) {
      if (element instanceof ExecutableElement methodElement) {
        final SourceMethod method = methodElement.getKind() == ElementKind.CONSTRUCTOR ?
            new SourceConstructorImpl(this.environment, methodElement, this) :
            new SourceMethodImpl(this.environment, methodElement, this);
        if (predicate.test(method)) {
          out.add(method);
        }
      }
    }
    return Collections.unmodifiableList(out);
  }

  @Override
  public List<SourceField> getNestedFields(final Predicate<SourceField> predicate) {
    final List<SourceField> out = new LinkedList<>();
    for (final Element element : this.element.getEnclosedElements()) {
      if (element.getKind() == ElementKind.FIELD && element instanceof VariableElement fieldElement) {
        final SourceField field = new SourceFieldImpl(this.environment, fieldElement, this);
        if (predicate.test(field)) {
          out.add(field);
        }
      }
    }
    return Collections.unmodifiableList(out);
  }

  @Override
  public List<SourceClass> getNestedClasses(final Predicate<SourceClass> predicate) {
    final List<SourceClass> out = new LinkedList<>();
    for (final Element element : this.element.getEnclosedElements()) {
      if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD) {
        final SourceClass nested = SourceTypeUtils.getSourceClassType(this.environment, (DeclaredType) element.asType());
        if (predicate.test(nested)) {
          out.add(nested);
        }
      }
    }
    return Collections.unmodifiableList(out);
  }

  @Override
  public <T extends Annotation> @Nullable T getAnnotation(final Class<T> type) {
    return this.element.getAnnotation(type);
  }

  @Override
  public List<SourceClass> getAllAnnotations() {
    return this.element.getAnnotationMirrors().stream()
        .map(mirror -> new SourceClassImpl(this.environment, mirror.getAnnotationType()))
        .map(SourceClass.class::cast)
        .toList();
  }

  @Override
  public @Nullable <T extends Annotation> SourceClass getAnnotationSourceClassField(final Class<T> type, final String fieldName) {
    return Optional.ofNullable(SourceTypeUtils.getAnnotationMirror(this.element, type, fieldName))
        .map(mirror -> new SourceClassImpl(this.environment, (DeclaredType) mirror))
        .orElse(null);
  }

  @Override
  public final String getPackageName() {
    return this.environment.getElementUtils().getPackageOf(this.getElement()).getQualifiedName().toString();
  }

  @Override
  public String getFullyQualifiedName() {
    return this.element.getQualifiedName().toString();
  }

  @Override
  public String getFullyQualifiedAndTypedName() {
    return this.type.toString();
  }

  @Override
  public String getSourceName() {
    final List<String> out = new LinkedList<>();
    out.add(getName());

    SourceClass thisClass = this;
    while (!thisClass.isTopLevel()) {
      thisClass = thisClass.getOuterClass();
      out.add(thisClass.getName());
    }

    return String.join(".", out.reversed());
  }

  @Override
  public String getName() {
    return this.element.getSimpleName().toString();
  }

  @Override
  public Set<Modifier> getModifiers() {
    return Collections.unmodifiableSet(this.element.getModifiers());
  }

  @Override
  public String toString() {
    return getName();
  }
}
