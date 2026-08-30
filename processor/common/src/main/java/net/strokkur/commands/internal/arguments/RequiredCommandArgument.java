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
package net.strokkur.commands.internal.arguments;

import net.strokkur.commands.internal.intermediate.attributes.AttributableHelper;
import net.strokkur.jap.code.type.CodeType;
import net.strokkur.jap.source.classmodel.SourceParameterLike;

import java.util.Map;
import java.util.TreeMap;

public class RequiredCommandArgument implements CommandArgument, AttributableHelper {
  private final BrigadierArgumentType argumentType;
  private final SourceParameterLike param;
  private final Map<String, Object> attributeMap = new TreeMap<>();

  public static RequiredCommandArgument of(BrigadierArgumentType argumentType, SourceParameterLike param) {
    return new RequiredCommandArgument(argumentType, param);
  }

  private RequiredCommandArgument(BrigadierArgumentType argumentType, SourceParameterLike param) {
    this.argumentType = argumentType;
    this.param = param;
  }

  public BrigadierArgumentType argumentType() {
    return argumentType;
  }

  @Override
  public String argumentName() {
    return param.name();
  }

  public CodeType parameterType() {
    return param.type().toType();
  }

  @Override
  public Map<String, Object> attributeMap() {
    return this.attributeMap;
  }

  @Override
  public String toString() {
    return "RequiredArg(%s)".formatted(argumentType);
  }
}
