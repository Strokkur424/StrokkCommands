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
package net.strokkur.commands.internal.velocity.util;

import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.CodeExpression;
import net.strokkur.jap.code.expression.Expressions;

public enum SenderType {
  NORMAL(VelocityClasses.COMMAND_SOURCE, Expressions.bool(true)),
  CONSOLE(VelocityClasses.CONSOLE_COMMAND_SOURCE, Expressions.variable("source").instanceOf(VelocityClasses.CONSOLE_COMMAND_SOURCE)),
  PLAYER(VelocityClasses.PLAYER, Expressions.variable("source").instanceOf(VelocityClasses.PLAYER));

  private final VelocityClasses classType;
  private final CodeExpression predicate;

  SenderType(VelocityClasses classType, ConvertToExpression predicate) {
    this.classType = classType;
    this.predicate = predicate.toExpression();
  }

  public VelocityClasses getClassType() {
    return classType;
  }

  public CodeExpression getPredicate() {
    return predicate;
  }
}
