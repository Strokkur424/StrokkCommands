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

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeField;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeParameter;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;

public interface CodeVisitor<R> {
  R visitClass(CodeClass codeClass);

  R visitMethod(CodeMethod codeMethod);

  R visitPackage(CodePackage codePackage);

  R visitParameter(CodeParameter codeParameter);

  R visitType(CodeType codeType);

  R visitAnnotation(CodeAnnotation codeAnnotation);

  R visitField(CodeField codeField);

  R visitExpression(CodeExpression codeExpression);

  R visitStatement(CodeStatement codeStatement);
}
