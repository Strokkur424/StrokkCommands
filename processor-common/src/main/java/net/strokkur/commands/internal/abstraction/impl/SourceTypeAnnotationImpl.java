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
import net.strokkur.commands.internal.abstraction.SourceTypeAnnotation;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.util.TypeKindVisitor14;
import java.util.Set;
import java.util.TreeSet;

public class SourceTypeAnnotationImpl implements SourceTypeAnnotation {
  private final ProcessingEnvironment environment;
  private final TypeParameterElement element;
  private @Nullable Set<String> imports = null;

  public SourceTypeAnnotationImpl(final ProcessingEnvironment environment, final TypeParameterElement element) {
    this.environment = environment;
    this.element = element;
  }

  @Override
  public String getName() {
    return this.element.asType().toString();
  }

  @Override
  public String getDefinitionString() {
    return Trees.instance(this.environment).getTree(this.element).toString();
  }

  @Override
  public Set<String> getImports() {
    if (this.imports != null) {
      return this.imports;
    }

    this.imports = new TreeSet<>();
    final TypeVisitor<Void, @Nullable Void> visitor = new TypeKindVisitor14<>() {
      @Override
      public Void visitDeclared(final DeclaredType t, final Void unused) {
        imports.addAll(new SourceClassImpl(environment, t).getImports());
        for (final TypeMirror typeArg : t.getTypeArguments()) {
          typeArg.accept(this, unused);
        }
        return super.DEFAULT_VALUE;
      }

      @Override
      public Void visitIntersection(final IntersectionType t, final Void unused) {
        for (final TypeMirror type : t.getBounds()) {
          type.accept(this, unused);
        }
        return super.DEFAULT_VALUE;
      }

      @Override
      public Void visitUnion(final UnionType t, final Void unused) {
        for (final TypeMirror type : t.getAlternatives()) {
          type.accept(this, unused);
        }
        return super.DEFAULT_VALUE;
      }

      @Override
      public Void visitTypeVariable(final TypeVariable t, final Void unused) {
        t.getUpperBound().accept(this, unused);
        t.getLowerBound().accept(this, unused);
        return super.DEFAULT_VALUE;
      }
    };

    this.element.asType().accept(visitor, null);
    return this.imports;
  }
}
