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
package net.strokkur.commands.annotations.arguments;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Configures a long argument type.
///
/// The following configurations are possible:
///   1. `min` set the lowest value a user is allowed to enter. Defaults to [Long#MIN_VALUE].
///   2. `max` set the highest value a user is allowed to enter. Defaults to [Long#MAX_VALUE].
///
/// The values are inclusive. Meaning if you want a user to only input the numbers `0, 1, ..., 10`,
/// you would want to choose `0` as the `min` and `10` as the `max`.
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(CommandSender sender, @LongArg(min = 0, max = 10) long value);
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface LongArg {
  /// {@return the lowest possible input value. Inclusive}
  long max() default Long.MAX_VALUE;

  /// {@return the highest possible input value. Inclusive}
  long min() default Long.MIN_VALUE;
}
