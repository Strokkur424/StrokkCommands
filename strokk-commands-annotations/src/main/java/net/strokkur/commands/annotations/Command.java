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
package net.strokkur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares a command. This is the annotation the annotation-processor listens to when generating commands.
///
/// This annotation can only be used on **top-level classes**, whose visibility is at least package-private.
/// You can find documentation about declaring commands with StrokkCommands in [the official documentation](https://commands.strokkur.net).
///
/// Requires entering a command name.
///
/// Example usage:
/// ```java
/// @Command("mycommand")
/// classMyCommand {
///   /* command definition here */
/// }
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Command {
  /// {@return the name of the command}
  String value();
}