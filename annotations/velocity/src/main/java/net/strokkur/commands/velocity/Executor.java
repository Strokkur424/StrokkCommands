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
package net.strokkur.commands.velocity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares that a parameter should not be interpreted as an argument type but
/// as the required command executor.
///
/// This annotation **can only be applied to** the second parameter of a method and requires
/// the type to be **a [com.velocitypowered.api.proxy.Player]**.
///
/// The executor is the entity, for which the command was executed. This can be a different object from
/// the [com.velocitypowered.api.command.CommandSource] if the command was ran with `/execute as` or a different `redirect`/`fork` method.
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(CommandSource source, @Executor Player player);
/// ```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Executor {
}
