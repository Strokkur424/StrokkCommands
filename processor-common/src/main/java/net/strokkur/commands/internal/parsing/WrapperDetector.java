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
package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.ExecutorWrapper;
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.intermediate.attributes.ExecutorWrapperProvider;
import net.strokkur.commands.internal.intermediate.attributes.ExecutorWrapperProvider.WrapperType;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for detecting executor wrapper annotations and their corresponding methods.
 */
public class WrapperDetector implements ForwardingMessagerWrapper {
  private final MessagerWrapper messager;

  public WrapperDetector(MessagerWrapper messager) {
    this.messager = messager;
  }

  /**
   * Detects if the given element has a wrapper annotation and finds the corresponding wrapper method.
   *
   * @param element    The element (class or method) to check for wrapper annotations
   * @param rootClass  The root command class to search for wrapper methods
   * @return Optional containing the wrapper provider if a valid wrapper is found
   */
  public Optional<ExecutorWrapperProvider> detectWrapper(
      final AnnotationsHolder element,
      final SourceClass rootClass
  ) {
    // Find wrapper annotations on this element (annotations that have @ExecutorWrapper)
    final Optional<SourceClass> wrapperAnnotation = element.getFirstAnnotationWithMetaAnnotation(ExecutorWrapper.class);
    if (wrapperAnnotation.isEmpty()) {
      return Optional.empty();
    }

    final String wrapperAnnotationFqn = wrapperAnnotation.get().getFullyQualifiedName();
    debug("  | Found wrapper annotation: {}", wrapperAnnotationFqn);

    // Find the wrapper method in the class hierarchy
    final SourceMethod wrapperMethod = findWrapperMethod(wrapperAnnotationFqn, rootClass);
    if (wrapperMethod == null) {
      error("Wrapper annotation {} is used but no wrapper method with this annotation was found in the command class hierarchy", wrapperAnnotationFqn);
      return Optional.empty();
    }

    // Determine wrapper type from method signature
    final WrapperType wrapperType = determineWrapperType(wrapperMethod);
    if (wrapperType == null) {
      error("Wrapper method {} has invalid signature. Expected one of: Command<S>(Command<S>, Method), int(CommandContext<S>, Command<S>, Method), void(CommandContext<S>, Command<S>, Method)", wrapperMethod.getName());
      return Optional.empty();
    }

    return Optional.of(new ExecutorWrapperProvider(
        wrapperMethod,
        wrapperAnnotationFqn,
        wrapperMethod.isStaticallyAccessible(),
        wrapperType
    ));
  }

  /**
   * Finds a method with the given wrapper annotation in the class hierarchy.
   */
  private @Nullable SourceMethod findWrapperMethod(final String wrapperAnnotationFqn, final SourceClass rootClass) {
    // Collect all methods from the class hierarchy
    final List<SourceMethod> allMethods = collectAllMethods(rootClass);

    // Find the method that has the wrapper annotation
    for (final SourceMethod method : allMethods) {
      for (final SourceClass annotation : method.getAllAnnotations()) {
        if (annotation.getFullyQualifiedName().equals(wrapperAnnotationFqn)) {
          return method;
        }
      }
    }

    return null;
  }

  /**
   * Collects all methods from a class and its nested classes.
   */
  private List<SourceMethod> collectAllMethods(final SourceClass sourceClass) {
    final List<SourceMethod> methods = new ArrayList<>(sourceClass.getNestedMethods());

    for (final SourceClass nestedClass : sourceClass.getNestedClasses()) {
      methods.addAll(collectAllMethods(nestedClass));
    }

    return methods;
  }

  /**
   * Determines the wrapper type based on the method's return type.
   */
  private @Nullable WrapperType determineWrapperType(final SourceMethod method) {
    final String returnTypeName = method.getReturnType().getFullyQualifiedName();

    // Check for Command<S> return type (com.mojang.brigadier.Command)
    if (returnTypeName.startsWith("com.mojang.brigadier.Command")) {
      return WrapperType.COMMAND_WRAPPER;
    }

    // Check for int return type
    if (returnTypeName.equals("int")) {
      return WrapperType.INT_EXECUTOR;
    }

    // Check for void return type
    if (returnTypeName.equals("void")) {
      return WrapperType.VOID_EXECUTOR;
    }

    return null;
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return messager;
  }
}
