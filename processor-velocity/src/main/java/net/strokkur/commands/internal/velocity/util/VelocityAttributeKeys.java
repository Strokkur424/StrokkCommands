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

import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.Set;

@NullMarked
public class VelocityAttributeKeys {
  public static final AttributeKey<SenderType> SENDER_TYPE = AttributeKey.create("sender_type", SenderType.NORMAL);
  public static final AttributeKey<Set<String>> PERMISSIONS = AttributeKey.createDynamic("permission", HashSet::new);

  private VelocityAttributeKeys() throws IllegalAccessError {
    throw new IllegalAccessError("This class cannot be instantiated");
  }
}
