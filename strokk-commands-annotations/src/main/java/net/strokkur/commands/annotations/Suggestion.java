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

import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares a suggestion provider for an argument.
///
/// - [#base()] declares the base class to look for the suggestion provider. Defaults to the current class.
///
/// - If [#field()] is set, it will that field (assuming its type is a [SuggestionProvider])
///   to set the suggestion provider on the argument. For `@Suggestion(field = "myProvider")`, the following
///   Brigadier code would get printed out:
///   ```java
///   Commands.argument("argname", /* type */)
///     .suggests(MyCommand.myProvider)
///   ```
///
/// - [#method()] does the same as [#field()], except that it calls the method to retrieve an instance of the
///   [SuggestionProvider]:
///   ```java
///   Commands.argument("argname", /* type */)
///     .suggests(MyCommand.myProvider())
///   ```
///
/// - [#reference()] declares that, if the method was set, it should be printed as a method reference (an implementing
///   method of the [SuggestionProvider] functional interface) instead of a method *returning* a [SuggestionProvider]:
///   ```java
///   Commands.argument("argname", /* type */)
///     .suggests(MyCommand::myProvider)
///   ```
///
/// If only the [#base()] class is provided, and it implements the [SuggestionProvider] interface, and instance
/// of that class will be passed to the `suggests` method instead:
/// ```java
/// Commands.argument("argname", /* type */)
///   .suggests(new ProvidedBaseClass())
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface Suggestion {
  /// {@return the base class to look for the provider}
  Class<?> base() default Class.class;

  /// {@return a field returning a suggestion provider inside the base class}
  String field() default "";

  /// {@return a method name inside the base class}
  ///
  /// The method should either return a [SuggestionProvider] (with [#reference()] set to `false`
  /// or implementing the [SuggestionProvider#getSuggestions(com.mojang.brigadier.context.CommandContext, com.mojang.brigadier.suggestion.SuggestionsBuilder)] method
  /// (with [#reference()] set to `true`).
  String method() default "";

  /// {@return whether to treat the [#method()] as a method reference or a method returning a suggestion provider}
  boolean reference() default true;
}
