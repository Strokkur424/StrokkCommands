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
package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.util.ConvertableTo;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record InvokesMethod(
    String methodName,
    CodeType.@Nullable ClassType type,
    List<CodeExpression> parameters,
    @Nullable String instanceVariable,
    boolean isStatic,
    boolean isCtor,
    StyleConfig style,
    List<Chained> chained
) implements ConvertableTo.Self<InvokesMethod> {

  public record Chained(
      String methodName,
      List<CodeExpression> parameters,
      StyleConfig style
  ) {
  }

  public record StyleConfig(
      boolean newline,
      boolean multilineParameters,
      boolean newlineClosingBrace
  ) {
    public static final StyleConfig DEFAULT = new StyleConfig(
        false, false, false
    );
    public static final StyleConfig NEWLINE = new StyleConfig(
        true, false, false
    );
    public static final StyleConfig MULTILINE = new StyleConfig(
        false, true, false
    );
    public static final StyleConfig NEWLINE_MULTILINE = new StyleConfig(
        true, true, false
    );
    public static final StyleConfig NEWLINE_BOTH = new StyleConfig(
        true, false, true
    );
  }
}
