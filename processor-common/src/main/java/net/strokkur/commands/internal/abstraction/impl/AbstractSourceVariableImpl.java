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

import com.sun.source.util.Trees;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceType;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public abstract class AbstractSourceVariableImpl<E extends Element> implements SourceVariable, ElementGettable<E> {
  protected final ProcessingEnvironment environment;
  protected final SourceType type;
  protected final E element;

  public AbstractSourceVariableImpl(final ProcessingEnvironment environment, final E element) {
    this.environment = environment;
    this.element = element;
    this.type = SourceTypeUtils.getSourceType(this.environment, element.asType());
  }

  @Override
  public E getElement() {
    return element;
  }

  public SourceType getType() {
    return this.type;
  }

  public String getName() {
    return this.element.getSimpleName().toString();
  }

  public String getFullDefinition() {
    return Trees.instance(this.environment).getTree(this.element).toString();
  }

  public Set<Modifier> getModifiers() {
    return this.element.getModifiers();
  }

  public <T extends Annotation> @Nullable T getAnnotation(final Class<T> type) {
    return element.getAnnotation(type);
  }

  @Override
  public List<SourceClass> getAllAnnotations() {
    return this.element.getAnnotationMirrors().stream()
        .map(mirror -> new SourceClassImpl(this.environment, mirror.getAnnotationType()))
        .map(SourceClass.class::cast)
        .toList();
  }

  @Override
  public String toString() {
    return getName();
  }
}
