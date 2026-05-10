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
package net.strokkur.commands.internal.codegen.builder;

import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.util.ConvertableTo;
import org.jspecify.annotations.Nullable;

public class FieldAccessBuilder implements ConvertableTo<CodeExpression.FieldAccess>, AsExpression {
  private final String fieldName;

  private CodeType.@Nullable ClassType type = null;
  private @Nullable CodeExpression source = null;
  private boolean isStatic = false;

  public FieldAccessBuilder(String fieldName) {
    this.fieldName = fieldName;
  }

  public FieldAccessBuilder setType(CodeType.ClassType type) {
    this.type = type;
    return this;
  }

  public FieldAccessBuilder setSource(AsExpression source) {
    this.source = source.getAsExpression();
    return this;
  }

  public FieldAccessBuilder setStatic(CodeType.ClassType type) {
    this.isStatic = true;
    this.type = type;
    return this;
  }

  public CodeExpression.FieldAccess build() {
    return new CodeExpression.FieldAccess(
        type, source, fieldName, isStatic
    );
  }

  @Override
  public CodeExpression.FieldAccess convert() {
    return build();
  }

  @Override
  public CodeExpression getAsExpression() {
    return build();
  }
}
