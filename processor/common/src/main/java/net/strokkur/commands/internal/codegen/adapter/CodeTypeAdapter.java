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
package net.strokkur.commands.internal.codegen.adapter;

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceType;
import net.strokkur.commands.internal.abstraction.SourceTypeVariable;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeType;

import java.util.List;

public final class CodeTypeAdapter {

  public static CodeType from(SourceType sourceType) {
    if (sourceType instanceof SourceTypeVariable typeVariable) {
      return CodeType.generic(typeVariable.getName(), null);
    }
    if (sourceType instanceof SourceClass sourceClass) {
      return from(sourceClass);
    }

    throw new IllegalStateException("No adapter mapping for " + sourceType.getClass().getName());
  }

  public static CodeType.ClassType from(SourceClass sourceType) {
    return CodeType.ofClass(CodeClass.nested(
        CodePackage.of(sourceType.getPackageName()),
        List.of(sourceType.getSourceName().split("\\."))
    ));
  }

  private CodeTypeAdapter() {
  }
}
