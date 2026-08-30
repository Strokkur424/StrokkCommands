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
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;

public final class PrototypeArgument extends PrototypeNode {
  final String name;
  final BrigadierArgumentType argumentType;

  PrototypeArgument(String name, BrigadierArgumentType argumentType) {
    this.name = name;
    this.argumentType = argumentType;
  }

  @Override
  boolean isArgumentPresent(String name) {
    if (this.name.equals(name)) {
      return true;
    }
    return super.isArgumentPresent(name);
  }

  @Override
  protected InvocationChainBuilder nodeElement() {
    return PlatformUtils.get().argumentBuilder(Expressions.string(name), argumentType.initializer());
  }

  @Override
  protected String commandStringElement() {
    return "<" + name + ">";
  }
}
