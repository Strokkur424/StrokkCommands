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

import net.strokkur.commands.StringArgType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Configures a [String] argument type.
///
/// There are three possible values.
/// [The specifics of each type can be looked up in the Paper docs](https://docs.papermc.io/paper/dev/command-api/basics/arguments-and-literals/#string-arguments).
/// Below is a quick recap of each type:
///   1. [StringArgType#WORD] a single word consisting of alphanumerical characters
///      and the special characters `+`, `-`, `_`, and `.`.
///   2. [StringArgType#STRING] same as the word type, but allows for all Unicode input (including spaces)
///      if quoted within double quotes `"`.
///   3. [StringArgType#GREEDY] allows for all input without any further validation. Includes all Unicode input (including spaces)
///      without the need to be quoted. Quotes `"` are interpreted literally. **This type must not be followed by any other arguments**.
///
/// **Defaults to [StringArgType#WORD]**.
///
/// For a better user experience, you can import the [StringArgType] statically:
/// ```java
/// import static net.strokkur.commands.StringArgType.GREEDY;
/// ```
///
/// Example usage:
/// ```java
/// @Executes
/// void executes(CommandSender sender, @StringArg(GREEDY) String message);
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface StringArg {
  /// The [StringArgType] value.
  ///
  /// **Defaults to [StringArgType#WORD]**.
  StringArgType value() default StringArgType.WORD;
}
