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
package net.strokkur.commands.internal.printer.command;

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.InvokesMethod;
import net.strokkur.commands.internal.codegen.Modifiers;
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitor;
import net.strokkur.commands.internal.printer.javadoc.AbstractJavadocPrintingVisitor;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractCommandTreePrintingVisitor implements CodeVisitor<StringBuilder> {
  protected final Supplier<AbstractJavadocPrintingVisitor> javadocPrintingVisitor;
  private final String indentString;
  private int indentation = 0;

  public AbstractCommandTreePrintingVisitor(Supplier<AbstractJavadocPrintingVisitor> javadocPrintingVisitor, String indentString) {
    this.javadocPrintingVisitor = javadocPrintingVisitor;
    this.indentString = indentString;
  }

  protected final void appendIndented(Runnable run) {
    incrementIndent();
    run.run();
    decrementIndent();
  }

  protected final StringBuilder appendIndented(Consumer<StringBuilder> run) {
    final StringBuilder builder = new StringBuilder();
    incrementIndent();
    run.accept(builder);
    decrementIndent();
    return builder;
  }

  protected final StringBuilder append(Consumer<StringBuilder> run) {
    final StringBuilder builder = new StringBuilder();
    run.accept(builder);
    return builder;
  }

  protected final void appendIndent(StringBuilder builder) {
    builder.repeat(indentString, indentation);
  }

  protected final void incrementIndent() {
    indentation++;
  }

  protected final void decrementIndent() {
    indentation--;
  }

  // Utility methods

  protected void appendNested(StringBuilder builder, CodeVisitable visitable) {
    builder.append(visitable.accept(this));
  }

  protected <S extends CodeVisitable> String joining(Collection<S> nested) {
    return nested.stream()
        .map(visitable -> visitable.accept(this))
        .map(StringBuilder::toString)
        .collect(Collectors.joining(", "));
  }

  protected void printJavadocIndented(StringBuilder builder, @Nullable CodeJavadoc javadoc) {
    if (javadoc != null) {
      final AbstractJavadocPrintingVisitor visitor = javadocPrintingVisitor.get();
      javadoc.accept(visitor);
      for (String line : visitor.getLines()) {
        appendIndent(builder);
        builder.append(line);
        builder.append("\n");
      }
    }
  }

  protected void printAnnotationsIndented(StringBuilder builder, List<CodeAnnotation> annotations) {
    for (CodeAnnotation annotation : annotations) {
      appendIndent(builder);
      appendNested(builder, annotation);
      builder.append("\n");
    }
  }

  protected void printModifiersIndented(StringBuilder builder, Set<Modifiers> modifiers) {
    appendIndent(builder);
    modifiers.stream()
        .sorted(Comparator.comparingInt(Modifiers::priority))
        .map(Modifiers::toString)
        .forEach(mod -> builder.append(mod).append(' '));
  }

  protected void appendMethodInvocation(StringBuilder builder, InvokesMethod method) {
    if (method.instanceVariable() != null) {
      builder.append(method.instanceVariable()).append(".");
    }
    builder.append(method.method().name());
    builder.append("(");
    builder.append(joining(method.parameters()));
    builder.append(")");
  }
}
