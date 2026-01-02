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
import java.lang.reflect.Method;

/// Declares that an annotation should be treated as a provider for executor wrappers.
///
/// Apply this annotation to your own custom annotation:
/// ```java
/// @CustomExecutorWrapper
/// @interface TimingWrapper {}
/// ```
///
/// ## Annotating the wrapper method
/// The wrapper method must be annotated with your custom wrapper annotation.
/// Generally, the wrapper method should be static, except if only used inside a single
/// command class, in which it may be non-static.
///
/// The wrapper method should have a `Command<S>` parameter, returning a `Command<S>`.
///
/// Additionally, the wrapper method may define an optional, second parameter to retrieve the
/// [Method] instance the wrapper was called for.
///
/// - `Command<S> wrapper(Command<S>)`
/// - `Command<S> wrapper(Command<S>, Method)`
///
/// ## Example usage
/// ```java
/// @TimingWrapper
/// static Command<CommandSourceStack> time(Command<CommandSourceStack> executor) {
///   return (ctx) -> {
///     final long start = System.nanoTime();
///     try {
///       return executor.run(ctx);
///     } finally {
///       LOGGER.info("Took {} nanoseconds!", System.nanoTime() - start);
///     }
///   };
/// }
/// ```
///
/// ## Using the custom executor wrapper annotation
/// Apply your wrapper annotation to classes or methods:
/// ```java
/// @Command("mycommand")
/// @TimingWrapper // Applies to all methods in this command
/// class MyCommand {
///
///   @Executes("fast")
///   void fastCommand(S source) {
///     // Defaults to @TimingWrapper
///   }
///
///   @Executes("admin")
///   @DifferentWrapper // Override with a different wrapper for this method
///   void adminCommand(S source) {
///     // ...
///   }
/// }
/// ```
///
/// @see UnsetExecutorWrapper
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CustomExecutorWrapper {
}
