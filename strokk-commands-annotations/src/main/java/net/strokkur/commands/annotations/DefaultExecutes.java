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
import java.util.List;

/// The default method to call if an invalid number of arguments were declared
///
/// This annotation acts very similar to the standard [Executes] annotation, with
/// the only difference being that it will also apply itself to all child notes.
///
/// So if you have a structure similar to this:
/// ```java
/// @Command("command")
/// class MyCommand {
///
///   @DefaultExecutes
///   void help(CommandSender sender, String[] extraArgs) {
///     sender.sendPlainMessage("/" + String.join(" ", extraArgs) + " is missing some arguments!")
///   }
///
///   @Executes("some literals")
///   void logic(CommandSender sender, int num, String word) {
///     // ...
///   }
/// }
/// ```
/// The following paths will be valid for execution:
/// ```
/// /command                             - MyCommand#help
/// /command some                        - MyCommand#help
/// /command some literals               - MyCommand#help
/// /command some literals <num>         - MyCommand#help
/// /command some literals <num> <word>  - MyCommand#logic
/// ```
///
/// You can add either a `String[]` or `List<String>` parameter to the end of the method parameters
/// to obtain all arguments (including the command name, without the slash) a user has provided. The [List] is **immutable**.
///
/// You can provide a literal path to precede to the [DefaultExecutes] method, exactly the same way as
/// with the [Executes] annotation. You can also add arguments the exactly same way.
///
/// If multiple [DefaultExecutes]-annotated methods are present, the **deeper one** in the tree takes precedence.
/// If multiple [DefaultExecutes]-annotated methods are present on the same path, the first declared one takes precedence.
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface DefaultExecutes {
  /// A literal path to prepend to the method.
  ///
  /// This
  /// ```java
  /// @DefaultExecutes("the literal path")
  /// void help(CommandSender sender, /* rest of arguments */);
  /// ```
  /// is the same as writing
  /// ```java
  /// @DefaultExecutes
  /// void help(CommandSender sender, @Literal("the literal path") String lit, /* rest of arguments */);
  /// ```
  ///
  /// @return the literal path to prepend to the argument path
  String value() default "";
}
