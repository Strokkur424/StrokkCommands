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
import java.util.function.Predicate;

/// Declares that an annotation should be treated as a provider for custom requirements.
///
/// You can apply this annotation to your own annotation types, like so:
/// ```java
/// @CustomRequirement
/// @interface YourProvider {}
/// ```
///
/// ## Annotating a predicate
/// Once you have done this, the next step is to annotate anything which can effectively return
/// a [Predicate]. The generic type of this provider might vary based on the platform
/// you are using.
///
/// The following can be annotated:
/// 1. A class implementing [Predicate]. Do note that currently, a parameterless constructor
///    **is strictly required**. An example implementation might look like this:
///    ```java
///    @YourProvider
///    class RequirementClass implements Predicate<S> {
///      @Override
///      public boolean test(final S source) {
///        return true;
///      }
///    }
///    ```
///    Make sure to replace `<S>` with the type appropriate for your platform.
///
/// 2. A **statically accessible** method following the semantic of the [Predicate#test(Object)]  method.
///    ```java
///    @YourProvider
///    static boolean methodImplementation(final S source) {
///      return true;
///    }
///    ```
///    This way is generally the easiest.
///
/// 3. A **statically accessible** method **returning** a [Predicate].
///    ```java
///    @YourProvider
///    static Predicate<S> requirementMethod() {
///      return (source) -> true;
///    }
///    ```
///
/// 4. A **statically accessible** field holding a [Predicate]. This field
///    **needs to be initialized** by the time you register the command.
///    ```java
///    @YourProvider
///    static Predicate<S> REQUIREMENT = (source) -> true;
///    ```
///
/// ## Using the custom requirement provider
/// Once you have annotated the predicate provider your annotation should point to, you can start using it.
/// It's as simple as adding the annotation anywhere you can put other
/// requirement annotations (command classes, executes-methods, fields, etc.)
///
/// ```java
/// @Executes
/// @YourProvider
/// void execute(S source) {
///   // ...
/// }
/// ```
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CustomRequirement {
}
