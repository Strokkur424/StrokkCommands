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

/// Declares that a method should be used as a reachable command path, interpreting its parameters are
/// command arguments and calling the method if those arguments were provided.
///
/// Also allows for declaring a literal path to prepend to the method.
///
/// Example usage:
/// ```java
/// @Executes("tree")
/// void executeTree(CommandSender sender, BlockPosition pos) {
///   // spawns a tree at <pos> if `/<command> tree` was executed
/// }
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Executes {
  /// A literal path to prepend to the method.
  ///
  /// This
  /// ```java
  /// @Executes("the literal path")
  /// void executes(CommandSender sender, /* rest of arguments */);
  /// ```
  /// is the same as writing
  /// ```java
  /// @Executes
  /// void executes(CommandSender sender, @Literal("the literal path") String lit, /* rest of arguments */);
  /// ```
  String value() default "";
}