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
package net.strokkur.commands.internal.printer.visitor;

import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SequencedCollection;

public class JavaStarJavadocVisitor extends AbstractJavadocPrintingVisitor {
  @Override
  public SequencedCollection<String> getLines() {
    final List<String> out = new ArrayList<>();
    out.add("/**");
    out.addAll(Arrays.stream(builder.toString().strip().split("\n"))
        .map(str -> str.isEmpty() ? " *" : " * " + str)
        .toList());
    out.add(" */");
    return out;
  }

  @Override
  public void visit(CodeJavadoc.PlainText value) {
    builder.append(value.text());
  }

  @Override
  public void visit(CodeJavadoc.Meta value) {
    builder
        .append('@')
        .append(value.descriptor())
        .append(' ')
        .append(value.value());
  }

  @Override
  public void visit(CodeJavadoc.MethodReferenceMeta value) {
    builder
        .append('@')
        .append(value.descriptor())
        .append(' ')
        .append(getMethodRefString(value.codeMethod(), value.localMethod()));

    if (value.text() != null) {
      builder.append(' ').append(value.text());
    }
  }

  @Override
  public void visit(CodeJavadoc.ClassReferenceMeta value) {
    builder
        .append('@')
        .append(value.descriptor())
        .append(' ')
        .append(value.codeClass().fullyQualifiedName());

    if (value.text() != null) {
      builder.append(' ').append(value.text());
    }
  }

  @Override
  public void visit(CodeJavadoc.Header value) {
    builder.append('\n');
    builder.append("<h").append(value.level()).append('>');
    builder.append(value.text());
    builder.append("</h").append(value.level()).append('>');
    builder.append('\n');
  }

  @Override
  public void visit(CodeJavadoc.Newline value) {
    builder.append('\n');
  }

  @Override
  public void visit(CodeJavadoc.Linebreak value) {
    builder.append("<p>");
  }

  @Override
  public void visit(CodeJavadoc.InlineCode value) {
    builder.append("{@code ").append(value.code()).append("}");
  }

  @Override
  public void visit(CodeJavadoc.CodeBlock value) {
    builder.append("<pre>{@code\n")
        .append(value.code())
        .append("\n}</pre>");
  }

  @Override
  public void visit(CodeJavadoc.Url value) {
    builder.append("<a href=\"").append(value.url()).append("\">")
        .append(value.text())
        .append("</a>");
  }

  @Override
  public void visit(CodeJavadoc.ClassReference value) {
    builder.append("{@link ").append(value.codeClass().fullyQualifiedName());
    if (value.name() != null) {
      builder.append(" ").append(value.name());
    }
    builder.append("}");
  }

  @Override
  public void visit(CodeJavadoc.MethodReference value) {
    builder.append("{@link ").append(getMethodRefString(value.method(), value.localMethod()));
    if (value.name() != null) {
      builder.append(" ").append(value.name());
    }
    builder.append("}");
  }

  protected String getMethodRefString(CodeMethod method, boolean local) {
    return local
        ? "#" + method.javadocName()
        : method.declaredClass().fullyQualifiedName() + "#" + method.javadocName();
  }
}
