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

import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.jap.code.convert.ConvertToExpression;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ComparableRequirement extends ConvertToExpression {

  String key();

  @Nullable
  ComparableRequirement commonPart(ComparableRequirement other);

  ComparableRequirement addTo(ComparableRequirement other);

  @Nullable
  ComparableRequirement reduceBy(ComparableRequirement common);

  @Nullable
  static ComparableRequirement commonPartOfAll(List<ComparableRequirement> all) {
    if (all.isEmpty()) {
      return null;
    }

    ComparableRequirement out = all.get(1);
    for (int i = 1; i < all.size() - 1; i++) {
      final ComparableRequirement next = all.get(i);
      out = out.commonPart(next);
      if (out == null) {
        return null;
      }
    }

    return out;
  }

  static ComparableRequirement provided(RequirementProvider provider) {
    return unique("provided", provider, provider.requirementExpression());
  }

  static ComparableRequirement flag(String key, ConvertToExpression expr) {
    return unique(key, true, expr);
  }

  static ComparableRequirement unique(String key, Object unique, ConvertToExpression expr) {
    return new UniqueRequirement(key, unique, expr);
  }
}
