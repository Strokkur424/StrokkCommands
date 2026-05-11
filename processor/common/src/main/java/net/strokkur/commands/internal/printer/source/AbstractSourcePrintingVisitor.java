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
package net.strokkur.commands.internal.printer.source;

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodePackage;
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

public abstract class AbstractSourcePrintingVisitor implements CodeVisitor<StringBuilder> {
  protected final Supplier<AbstractJavadocPrintingVisitor> javadocPrintingVisitor;
  private final String indentString;
  private final String continuationIndentString;
  private int indentation = 0;
  private int continuationIndent = 0;

  public AbstractSourcePrintingVisitor(Supplier<AbstractJavadocPrintingVisitor> javadocPrintingVisitor, String indentString, String continuationIndentString) {
    this.javadocPrintingVisitor = javadocPrintingVisitor;
    this.indentString = indentString;
    this.continuationIndentString = continuationIndentString;
  }

  /// Packages are never printed.
  @Override
  public final StringBuilder visitPackage(CodePackage codePackage) {
    throw new IllegalStateException("This should not be called.");
  }

  protected final void appendIndented(Runnable run) {
    indentation++;
    run.run();
    indentation--;
  }

  protected final void appendIndentedContinuation(Runnable run) {
    continuationIndent++;
    run.run();
    continuationIndent--;
  }

  protected final StringBuilder append(Consumer<StringBuilder> run) {
    final StringBuilder builder = new StringBuilder();
    run.accept(builder);
    return builder;
  }

  protected final void appendIndent(StringBuilder builder) {
    builder.repeat(indentString, indentation).repeat(continuationIndentString, continuationIndent);
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

  protected void appendMethodParametersMultiline(StringBuilder builder, List<CodeExpression> parameters) {
    appendIndentedContinuation(() -> {
      builder.append("\n");
      for (int i = 0, parametersSize = parameters.size(); i < parametersSize; i++) {
        final CodeExpression parameter = parameters.get(i);
        appendIndent(builder);
        appendNested(builder, parameter);
        if (i + 1 < parametersSize) {
          builder.append(",");
        }
        builder.append("\n");
      }
    });
    appendIndent(builder);
  }

  protected void appendMethodInvocation(StringBuilder builder, InvokesMethod method) {
    if (method.isCtor()) {
      builder.append("new ");
    } else {
      final String source;
      if (method.instanceSource() != null) {
        source = method.instanceSource().accept(this).toString();
      } else if (method.isStatic() && method.type() != null) {
        source = method.type().name();
      } else {
        source = null;
      }

      if (source != null) {
        builder.append(source);
        appendIndentedContinuation(() -> {
          if (method.style().newline()) {
            builder.append("\n");
            appendIndent(builder);
          }
          builder.append(".");
        });
      }
    }

    builder.append(method.methodName());
    builder.append("(");
    if (method.style().multilineParameters()) {
      appendMethodParametersMultiline(builder, method.parameters());
    } else {
      builder.append(joining(method.parameters()));
    }
    builder.append(")");

    appendIndentedContinuation(() -> {
      for (InvokesMethod.Chained chained : method.chained()) {
        if (chained.style().newline()) {
          builder.append("\n");
          appendIndent(builder);
        }
        builder.append(".");
        builder.append(chained.methodName());
        builder.append("(");
        if (chained.style().multilineParameters()) {
          appendMethodParametersMultiline(builder, chained.parameters());
        } else {
          builder.append(joining(chained.parameters()));
        }

        if (chained.style().newlineClosingBrace()) {
          builder.append("\n");
          appendIndent(builder);
        }
        builder.append(")");
      }
    });
  }
}
