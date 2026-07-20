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
package net.strokkur.commands.internal.prototype;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;

public final class PrototypeArgument extends PrototypeNode {
  private final String name;
  private final ConvertToExpression initializer;

  PrototypeArgument(String name, ConvertToExpression initializer) {
    this.name = name;
    this.initializer = initializer;
  }

  @Override
  protected InvocationChainBuilder nodeElement() {
    return PlatformUtils.get().argumentBuilder(Expressions.string(name), initializer);
  }

  @Override
  protected String commandStringElement() {
    return "<" + name + ">";
  }
}
