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

/// Declares one or more literal value paths for an argument.
///
/// If no parameter is provided to this annotation, it uses the
/// argument name as the literal. This
/// ```java
/// @Executes
/// void execute(CommandSender sender, @Literal String hi);
/// ```
/// is the same as writing
/// ```java
/// @Executes
/// void execute(CommandSender sender, @Literal("hi") String theNameDoesNotMatter);
/// ```
///
/// This annotation also allows for declaring multiple literals;
/// ```java
/// @Executes
/// void executeChoice(CommandSender sender, @Literal({"fly", "die"}) String choice) {
///   switch (choice) {
///     case "fly" -> /* handle fly */
///     case "die" -> /* handle die */
///     default -> throw new IllegalStateException("this will never be called");
///   }
/// }
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface Literal {
  /// {@return the literal path(s) to insert at the argument position}
  String[] value() default {};
}
