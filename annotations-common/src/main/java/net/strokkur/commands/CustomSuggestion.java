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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Declares that an annotation should be treated as a provider for custom suggestions.
///
/// You can apply this annotation to your own annotation types, like so:
/// ```java
/// @CustomSuggestion
/// @interface YourProvider {}
/// ```
///
/// ## Annotating a suggestion provider
/// Once you have done this, the next step is to annotate anything which can effectively return
/// a [SuggestionProvider]. The generic type of this provider might vary based on the platform
/// you are using.
///
/// The following can be annotated:
/// 1. A class implementing [SuggestionProvider]. Do note that currently, a parameterless constructor
///    **is strictly required**. An example implementation might look like this:
///    ```java
///    @YourProvider
///    class ClassSuggestionProvider implements SuggestionProvider<S> {
///      @Override
///      public CompletableFuture<Suggestions> getSuggestions(
///          final CommandContext<S> ctx,
///          final SuggestionsBuilder builder
///      ) throws CommandSyntaxException {
///        return builder.buildFuture();
///      }
///    }
///    ```
///    Make sure to replace `<S>` with the type appropriate for your platform.
///
/// 2. A **statically accessible** method following the semantic of the [SuggestionProvider#getSuggestions(CommandContext, SuggestionsBuilder)] method.
///    ```java
///    @YourProvider
///    static CompletableFuture<Suggestions> methodImplementation(
///        final CommandContext<S> ctx,
///        final SuggestionsBuilder builder
///    ) throws CommandSyntaxException {
///      return builder.buildFuture();
///    }
///    ```
///    This way is generally the easiest.
///
/// 3. A **statically accessible** method **returning** a [SuggestionProvider].
///    ```java
///    @YourProvider
///    static SuggestionProvider<S> methodSuggestionProvider() {
///      return (ctx, builder) -> builder.buildFuture();
///    }
///    ```
///
/// 4. A **statically accessible** field holding a [SuggestionProvider]. This field
///    **needs to be initialized** by the time you register the command.
///    ```java
///    @YourProvider
///    static SuggestionProvider<S> FIELD_PROVIDER = (ctx, builder) -> builder.buildFuture();
///    ```
///
/// ## Using the custom suggestion annotation
/// Once you have annotated the suggestion provider your annotation should point to, you can start using it
/// in your executes-methods. It's as simple as adding the annotation to a parameter.
///
/// ```java
/// @Executes
/// void execute(S source, @YourProvider String word) {
///   // ...
/// }
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CustomSuggestion {
}
