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
package net.strokkur.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares that a parameter should be injected with the Brigadier
/// {@code CommandContext} instead of being interpreted as a command argument.
///
/// This annotation can be applied to any parameter of type {@code CommandContext<?>}
/// in a method annotated with {@link Executes} or {@link DefaultExecutes}.
///
/// Example usage:
/// ```java
/// @Executes
/// void execute(CommandSender sender, @Context CommandContext<?> ctx, int amount);
/// ```
///
/// The annotated parameter will receive the raw Brigadier {@code CommandContext} at runtime,
/// allowing access to lower-level command execution details such as input parsing,
/// argument nodes, and the command source.
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface Context {}
