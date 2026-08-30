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
package net.strokkur.commands.internal.prototype.requirements;

import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.CodeExpression;
import org.jspecify.annotations.Nullable;

record UniqueRequirement(String key, Object unique, ConvertToExpression expr) implements ComparableRequirement {

  private boolean isSame(ComparableRequirement other) {
    return other instanceof UniqueRequirement(String otherKey, Object otherUnique, ConvertToExpression otherExpr)
      && unique.equals(otherUnique);
  }

  @Override
  public @Nullable ComparableRequirement reduceBy(ComparableRequirement common) {
    return this.isSame(common) ? null : this;
  }

  @Override
  public ComparableRequirement addTo(ComparableRequirement other) {
    throw new UnsupportedOperationException("Cannot add unique requirements together");
  }

  @Override
  public @Nullable ComparableRequirement commonPart(ComparableRequirement other) {
    return isSame(other) ? this : null;
  }

  @Override
  public CodeExpression toExpression() {
    return expr.toExpression();
  }
}
