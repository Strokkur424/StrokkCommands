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
package net.strokkur.commands.internal.intermediate.executable;

import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.util.Classes;
import org.jspecify.annotations.Nullable;

public interface DefaultExecutable extends Executable, Attributable {

  enum Type {
    NONE(null),
    ARRAY(Builders.methodInvocation("getInput").setInstanceVariable("ctx").chain("split", CodeExpression.string(" "))),
    LIST(Builders.methodInvocation("unmodifiableList").setStatic(Classes.COLLECTIONS)
        .addParameter(Builders.methodInvocation("asList").setStatic(Classes.ARRAYS)
            .addParameter(Builders.methodInvocation("getInput")
                .setInstanceVariable("ctx")
                .chain("split", CodeExpression.string(" "))
            ))
    );

    private final @Nullable AsExpression getter;

    Type(@Nullable AsExpression getter) {
      this.getter = getter;
    }

    public @Nullable AsExpression getGetter() {
      return this.getter;
    }

    public static DefaultExecutable.Type getType(SourceVariable variable) {
      if (variable.getType().getFullyQualifiedAndTypedName().equals("java.util.List<java.lang.String>")) {
        return LIST;
      }
      if (variable.getType().getFullyQualifiedName().equals("java.lang.String[]")) {
        return ARRAY;
      }
      return NONE;
    }
  }
}
