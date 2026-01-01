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

/// Meta-annotation that marks a custom annotation as an executor wrapper.
///
/// Use this to create reusable wrapper annotations that can be applied to command classes,
/// subcommands, or individual methods. The wrapper method is identified by also being
/// annotated with your custom wrapper annotation.
///
/// ## Creating a wrapper annotation
/// ```java
/// @ExecutorWrapper
/// @Retention(RetentionPolicy.RUNTIME)
/// @Target({ElementType.TYPE, ElementType.METHOD})
/// public @interface TimingWrapper {}
/// ```
///
/// ## Defining the wrapper method
/// The wrapper method must be annotated with your custom wrapper annotation.
/// It can be static or instance, and can have one of these signatures:
///
/// ### Full wrapper (returns `Command<S>`)
/// ```java
/// @TimingWrapper
/// static Command<CommandSourceStack> wrap(Command<CommandSourceStack> executor, Method method) {
///     return ctx -> {
///         long start = System.nanoTime();
///         try {
///             return executor.run(ctx);
///         } finally {
///             System.out.println("Took " + (System.nanoTime() - start) + "ns");
///         }
///     };
/// }
/// ```
///
/// ### Direct executor (returns `int`)
/// ```java
/// @TimingWrapper
/// static int execute(CommandContext<CommandSourceStack> ctx, Command<CommandSourceStack> executor, Method method) {
///     long start = System.nanoTime();
///     try {
///         return executor.run(ctx);
///     } finally {
///         System.out.println("Took " + (System.nanoTime() - start) + "ns");
///     }
/// }
/// ```
///
/// ## Applying the wrapper
/// Apply your wrapper annotation to classes or methods:
/// ```java
/// @Command("mycommand")
/// @TimingWrapper  // Applies to all methods in this command
/// class MyCommand {
///
///     @TimingWrapper  // The wrapper method definition
///     static Command<CommandSourceStack> wrap(Command<CommandSourceStack> executor, Method method) {
///         // ...
///     }
///
///     @Executes("fast")
///     void fastCommand(CommandSender sender) {
///         // Uses TimingWrapper
///     }
///
///     @Subcommand("admin")
///     @DifferentWrapper  // Override with a different wrapper for this subcommand
///     class AdminCommands {
///         // ...
///     }
/// }
/// ```
///
/// ## Wrapper method signatures
/// - `Command<S> wrap(Command<S> executor, Method method)` - Full wrapper, most flexible
/// - `int execute(CommandContext<S> ctx, Command<S> executor, Method method)` - Direct execution with result
///
/// The wrapper method can be:
/// - Static or instance (static is recommended for reusability)
/// - Defined in the command class, a subcommand class, or any other class
///
/// @see Executes
/// @see DefaultExecutes
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ExecutorWrapper {
}
