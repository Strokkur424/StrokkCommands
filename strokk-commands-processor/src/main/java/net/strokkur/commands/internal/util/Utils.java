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
package net.strokkur.commands.internal.util;

import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.util.TypeKindVisitor14;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Utils {

  @Nullable
  static TypeMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
    return getAnnotationMirror(element, annotationClass, "value");
  }

  @Nullable
  static TypeMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass, String fieldName) {
    String annotationName = annotationClass.getName();

    Optional<? extends AnnotationMirror> out = element.getAnnotationMirrors().stream()
        .filter(mirror -> ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationName))
        .findFirst();

    return out.flatMap(mirror -> mirror.getElementValues().entrySet().stream()
            .filter(entry -> entry.getKey().getSimpleName().contentEquals(fieldName))
            .map(entry -> (TypeMirror) entry.getValue().getValue())
            .findFirst())
        .orElse(null);
  }

  static PackageElement getPackageElement(TypeElement typeElement) {
    if (typeElement.getNestingKind() == NestingKind.TOP_LEVEL) {
      return (PackageElement) typeElement.getEnclosingElement();
    }

    return getPackageElement((TypeElement) typeElement.getEnclosingElement());
  }

  static String getTypeName(TypeMirror typeMirror) {
    return getTypeName(StrokkCommandsProcessor.getTypes().asElement(typeMirror));
  }

  static String getTypeName(Element type) {
    final StringBuilder builder = new StringBuilder();
    final List<String> names = getNestedClassNames(type);

    for (int i = 0, size = names.size(); i < size; i++) {
      final String name = names.get(i);
      builder.append(name);
      if (i + 1 < size) {
        builder.append(".");
      }
    }
    return builder.toString();
  }

  static List<String> getNestedClassNames(Element type) {
    final List<String> names = new ArrayList<>(16);

    do {
      names.add(type.getSimpleName().toString());
      type = type.getEnclosingElement();
    } while (type instanceof TypeElement);

    return names.reversed();
  }

  static String getInstanceName(List<ExecuteAccess<?>> stack) {
    final StringBuilder builder = new StringBuilder("instance");
    for (int i = 1, executeAccessStackSize = stack.size(); i < executeAccessStackSize; i++) {
      final ExecuteAccess<?> access = stack.get(i);
      final String name = access.getElement().getSimpleName().toString();
      builder.append(name.substring(0, 1).toUpperCase()).append(name.substring(1));
    }

    return builder.toString();
  }

  static String getInstanceName(TypeElement typeElement) {
    final StringBuilder builder = new StringBuilder();
    populateInstanceName(builder, typeElement);
    return builder.toString();
  }

  static String getFullyQualifiedName(TypeElement element) {
    return getPackageElement(element) + "." + getTypeName(element);
  }

  static void populateInstanceName(StringBuilder builder, TypeElement element) {
    if (element.getNestingKind().isNested()) {
      populateInstanceName(builder, (TypeElement) element.getEnclosingElement());
      builder.append(element.getSimpleName());
      return;
    }

    builder.append("instance");
  }

  static int getNestingCount(TypeElement typeElement) {
    int nested = 0;
    while (typeElement.getNestingKind() != NestingKind.TOP_LEVEL) {
      typeElement = (TypeElement) typeElement.getEnclosingElement();
      nested++;
    }
    return nested;
  }

  static boolean isFieldInitialized(VariableElement fieldElement) {
    final Trees trees = StrokkCommandsProcessor.getTrees();
    final VariableTree fieldTree = (VariableTree) trees.getTree(fieldElement);
    return fieldTree.getInitializer() != null;
  }

  static void populateParameterImports(final Set<String> imports, final Element element) {
    final TypeVisitor<Void, @Nullable Void> visitor = new TypeKindVisitor14<>() {
      @Override
      public Void visitDeclared(final DeclaredType t, final Void unused) {
        imports.add(getFullyQualifiedName((TypeElement) t.asElement()));
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

    element.asType().accept(visitor, null);
  }

  static String getMethodTypeParameterString(final ExecutableElement methodElement) {
    final List<String> typeParameters = new LinkedList<>();

    for (final TypeParameterElement typeParameter : ((TypeElement) methodElement.getEnclosingElement()).getTypeParameters()) {
      typeParameters.add(StrokkCommandsProcessor.getTrees().getTree(typeParameter).toString());
    }
    for (final TypeParameterElement typeParameter : methodElement.getTypeParameters()) {
      typeParameters.add(StrokkCommandsProcessor.getTrees().getTree(typeParameter).toString());
    }

    if (typeParameters.isEmpty()) {
      return "";
    }
    return " <" + String.join(", ", typeParameters) + ">";
  }

  static List<String> getParameterStrings(final List<? extends VariableElement> elements) {
    final List<String> variables = new LinkedList<>();
    for (final VariableElement parameter : elements) {
      variables.add(StrokkCommandsProcessor.getTrees().getTree(parameter).toString());
    }
    return variables;
  }
}