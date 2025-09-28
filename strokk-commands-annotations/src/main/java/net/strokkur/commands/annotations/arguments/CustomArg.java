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

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares that an executes method parameter should use
/// the here declared custom argument type for the argument.
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(CommandSender sender, @CustomArg(MyCustomArgument.class) CustomType custom);
/// ```
///
/// The value **must** implement [CustomArgumentType].
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface CustomArg {
  /// The [CustomArgumentType] to map to the annotated parameter.
  Class<? extends CustomArgumentType<?, ?>> value();
}
