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
package net.strokkur.commands.internal.velocity;

import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.exceptions.ConversionException;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.Nullable;

final class VelocityBrigadierArgumentConverter extends BrigadierArgumentConverter {
  VelocityBrigadierArgumentConverter(MessagerWrapper messagerWrapper) {
    super(messagerWrapper);
  }

  /// We do not support custom arguments for Velocity... yet!
  @Override
  protected @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(
      String argumentName, String type, SourceVariable parameter
  ) throws ConversionException {
    return null;
  }
}
