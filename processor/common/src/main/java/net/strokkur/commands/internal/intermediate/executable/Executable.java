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
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceMethod;
import net.strokkur.jap.source.type.SourcePrimitiveType;
import net.strokkur.jap.source.type.SourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Executable implements Parameterized, AttributableHelper {
  protected final SourceClassLike sourceClass;
  private final SourceMethod executesMethod;
  private final List<CommandParameter> parameters;
  private final Map<String, Object> attributeMap;
  private final ReturnType returnType;

  public Executable(SourceClassLike sourceClass, SourceMethod executesMethod, List<CommandParameter> parameters)
    throws IllegalReturnTypeException {
    this.sourceClass = sourceClass;
    this.executesMethod = executesMethod;
    this.parameters = List.copyOf(parameters);
    this.attributeMap = new HashMap<>();
    this.returnType = ReturnType.getType(executesMethod.returnType());
  }

  public Executable(Executable from) {
    this.sourceClass = from.sourceClass;
    this.executesMethod = from.executesMethod;
    this.parameters = List.copyOf(from.parameters);
    this.attributeMap = new HashMap<>(from.attributeMap);
    this.returnType = from.returnType;
  }

  public SourceMethod executesMethod() {
    return this.executesMethod;
  }

  public ReturnType returnType() {
    return this.returnType;
  }

  @Override
  public Map<String, Object> attributeMap() {
    return this.attributeMap;
  }

  @Override
  public List<CommandParameter> parameters() {
    return this.parameters;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + this.executesMethod + ']';
  }

  public enum ReturnType {
    INT,
    VOID;

    public static ReturnType getType(SourceType type) throws IllegalReturnTypeException {
      if (type == SourcePrimitiveType.VOID) {
        return VOID;
      }
      if (type == SourcePrimitiveType.INT) {
        return INT;
      }
      throw new IllegalReturnTypeException(type);
    }
  }
}
