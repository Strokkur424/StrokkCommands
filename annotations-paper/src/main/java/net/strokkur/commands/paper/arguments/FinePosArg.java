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
package net.strokkur.commands.paper.arguments;

import io.papermc.paper.math.FinePosition;

/// Configures a [FinePosition] argument.
///
/// The value of this annotation specifies whether
/// integer input (for example: `25 0 21`) gets centered to `.5`
/// or whether it is left as `.0`, which would point towards the
/// corner of a block. **Defaults to `false`**.
///
/// This only affects the x/z coordinates.
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(CommandSender sender, @FinePosArg(true) FinePosition pos);
/// ```
public @interface FinePosArg {
  /// {@return whether to center integer x/z values to the center of the block}
  boolean value() default false;
}
