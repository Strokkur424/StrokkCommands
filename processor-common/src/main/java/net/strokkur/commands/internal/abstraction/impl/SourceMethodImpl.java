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
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceType;
import net.strokkur.commands.internal.abstraction.SourceTypeAnnotation;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public sealed class SourceMethodImpl implements SourceMethod, ElementGettable<ExecutableElement> permits SourceConstructorImpl {
  private final ProcessingEnvironment environment;
  private final ExecutableElement element;
  private final SourceClass enclosed;

  public SourceMethodImpl(ProcessingEnvironment environment, ExecutableElement element, SourceClass enclosed) {
    this.environment = environment;
    this.element = element;
    this.enclosed = enclosed;
  }

  @Override
  public SourceType getReturnType() {
    return SourceTypeUtils.getSourceType(this.environment, this.element.getReturnType());
  }

  @Override
  public ExecutableElement getElement() {
    return this.element;
  }

  @Override
  public List<SourceParameter> getParameters() {
    final List<SourceParameter> out = new LinkedList<>();
    for (final VariableElement parameter : this.element.getParameters()) {
      out.add(new SourceParameterImpl(this.environment, parameter, this));
    }
    return out;
  }

  @Override
  public List<SourceTypeAnnotation> getTypeAnnotations() {
    final List<SourceTypeAnnotation> out = new ArrayList<>(this.element.getTypeParameters().size());
    for (final TypeParameterElement typeParameter : this.element.getTypeParameters()) {
      out.add(new SourceTypeAnnotationImpl(this.environment, typeParameter));
    }
    return out;
  }

  @Override
  public String getName() {
    return this.element.getSimpleName().toString();
  }

  @Override
  public Set<Modifier> getModifiers() {
    return this.element.getModifiers();
  }

  @Override
  public SourceClass getEnclosed() {
    return this.enclosed;
  }

  @Override
  public <T extends Annotation> @Nullable T getAnnotation(final Class<T> type) {
    return this.element.getAnnotation(type);
  }

  @Override
  public String toString() {
    return getEnclosed() + "#" + getName();
  }
}
