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

/// Declares that a method should be used to wrap all command executors in this command class.
///
/// The annotated method will be called for every `.executes()` call in the generated Brigadier
/// command tree, allowing you to intercept command execution, add logging, handle exceptions,
/// or read custom annotations from the command method.
///
/// Example usage:
/// ```java
/// @Command("mycommand")
/// class MyCommand {
///
///   @ExecutorWrapper
///   public Command<CommandSourceStack> wrap(Command<CommandSourceStack> executor, Method method) {
///     return ctx -> {
///       // Check for custom annotations on the method
///       if (method.isAnnotationPresent(MyCustomAnnotation.class)) {
///         // Do something special before execution
///       }
///
///       long start = System.currentTimeMillis();
///       try {
///         // Execute the original command
///         return executor.run(ctx);
///       } finally {
///         long duration = System.currentTimeMillis() - start;
///         System.out.println("Command " + method.getName() + " took " + duration + "ms");
///       }
///     };
///   }
///
///   @Executes("test")
///   void test(CommandSender source) {
///     source.sendMessage("Hello!");
///   }
/// }
/// ```
///
/// ## Method signature
/// The wrapper method must have the following signature:
/// ```java
/// Command<S> wrap(Command<S> executor, Method method)
/// ```
///
/// Where `S` is the platform-specific command source type (e.g., `CommandSourceStack` for Paper).
///
/// - `executor`: A Brigadier [com.mojang.brigadier.Command] that, when called, executes the original command logic
/// - `method`: The [java.lang.reflect.Method] reference to the original `@Executes` or `@DefaultExecutes` annotated method
///
/// The method returns a [com.mojang.brigadier.Command] which wraps the original executor. This allows you to:
/// - Add pre/post execution logic
/// - Handle exceptions
/// - Access method annotations at execution time
/// - Implement custom metrics or logging
///
/// ## Exception handling
/// The wrapper can catch and handle exceptions from the original executor.
/// `CommandSyntaxException` should be re-thrown for proper Brigadier error handling.
///
/// ## Scope
/// The wrapper applies to all `@Executes` and `@DefaultExecutes` methods within the same
/// command class and its nested subcommand classes.
///
/// @see Executes
/// @see DefaultExecutes
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ExecutorWrapper {
}
