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
package net.strokkur.commands.internal.codegen.visitor;

import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;

public interface JavadocVisitor {

  void visit(CodeJavadoc.PlainText value);

  void visit(CodeJavadoc.Meta value);

  void visit(CodeJavadoc.MethodReferenceMeta value);

  void visit(CodeJavadoc.ClassReferenceMeta value);

  void visit(CodeJavadoc.Header value);

  void visit(CodeJavadoc.Newline value);

  void visit(CodeJavadoc.Linebreak value);

  void visit(CodeJavadoc.InlineCode value);

  void visit(CodeJavadoc.CodeBlock value);

  void visit(CodeJavadoc.Url value);

  void visit(CodeJavadoc.ClassReference value);

  void visit(CodeJavadoc.MethodReference value);
}
