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
package net.strokkur.commands.paper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a parameter with a type compatible for [`@Executor`][Executor]
/// as **optional** (same behavior as a parameter typed `Optional<?>`) and defaults to using the command's
/// **executor**, if the command was run without providing an explicit value.
///
/// Example usage:
/// ```
/// @Executes("give-item")
/// void giveItem(CommandSender sender, @DefaultToExecutor Player target) {
///   target.give(constructItem());
///   sender.sendPlainMessage(target.getName() + " was given the item!");
/// }
/// ```
///
/// This would allow a player to execute `/<cmd> give-item` to give the item to himself
/// or `/<cmd> give-item <player>` to give the item to someone else.
///
/// @see Executor
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface DefaultToExecutor {
}
