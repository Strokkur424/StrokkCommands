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

import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;

import java.util.Arrays;
import java.util.SequencedCollection;

public class JavaMarkdownJavadocVisitor extends JavaStarJavadocVisitor {
  @Override
  public SequencedCollection<String> getLines() {
    return Arrays.stream(builder.toString().strip().split("\n"))
        .map(str -> str.isBlank() ? "" : " " + str)
        .map(str -> "///" + str)
        .toList();
  }

  @Override
  public void visit(CodeJavadoc.Header value) {
    builder.append('\n');
    builder.repeat("#", value.level()).append(' ');
    builder.append(value.text());
    builder.append('\n');
  }

  @Override
  public void visit(CodeJavadoc.Linebreak value) {
    // '<p>' in legacy JD; Markdown equivalent is to just keep the line empty.
  }

  @Override
  public void visit(CodeJavadoc.InlineCode value) {
    builder.append('`').append(value.code()).append('`');
  }

  @Override
  public void visit(CodeJavadoc.CodeBlock value) {
    builder.append("```\n");
    builder.append(value.code());
    builder.append("\n```");
  }

  @Override
  public void visit(CodeJavadoc.Url value) {
    builder.append('[').append(value.text()).append(']');
    builder.append('(').append(value.url()).append(')');
  }

  @Override
  public void visit(CodeJavadoc.ClassReference value) {
    if (value.name() != null) {
      builder.append('[').append(value.name()).append(']');
    }

    builder.append('[').append(value.codeClass().fullyQualifiedName()).append(']');
  }

  @Override
  public void visit(CodeJavadoc.MethodReference value) {
    if (value.name() != null) {
      builder.append('[').append(value.name()).append(']');
    }

    builder.append('[').append(getMethodRefString(value.method(), value.localMethod())).append(']');
  }
}
