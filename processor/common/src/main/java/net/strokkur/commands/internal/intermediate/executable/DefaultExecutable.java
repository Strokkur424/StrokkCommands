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

import net.strokkur.commands.internal.exceptions.IllegalReturnTypeException;
import net.strokkur.commands.internal.intermediate.attributes.AttributableHelper;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.CodeExpression;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.type.CodeType;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceMethod;
import net.strokkur.jap.source.type.SourceType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class DefaultExecutable extends Executable implements AttributableHelper {

  public DefaultExecutable(SourceClassLike sourceClass, SourceMethod executesMethod, List<CommandParameter> parameters)
    throws IllegalReturnTypeException {
    super(sourceClass, executesMethod, parameters);
  }

  public DefaultExecutable(Executable executable) {
    super(executable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      sourceClass,
      executesMethod()
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DefaultExecutable other) {
      return Objects.equals(sourceClass, other.sourceClass)
        && Objects.equals(executesMethod(), other.executesMethod());
    }
    return false;
  }

  public enum Type {
    NONE(null),
    ARRAY(Expressions.variable("ctx").chainMethod("getInput").chainMethod("split", Expressions.string(" "))),
    LIST(JavaTypes.COLLECTIONS.chainMethod("unmodifiableList")
      .addParameters(JavaTypes.ARRAYS.chainMethod("asList", ARRAY.getter()))
    );

    private final @Nullable CodeExpression getter;

    Type(@Nullable ConvertToExpression getter) {
      this.getter = getter != null ? getter.toExpression() : null;
    }

    public CodeExpression getter() {
      return Objects.requireNonNull(this.getter);
    }

    public static DefaultExecutable.Type getType(SourceType type) {
      CodeType codeType = type.toType();
      if (JavaTypes.LIST.typed(JavaTypes.STRING).equals(codeType)) {
        return LIST;
      }
      if (JavaTypes.STRING.toArray().equals(codeType)) {
        return ARRAY;
      }
      return NONE;
    }
  }
}
