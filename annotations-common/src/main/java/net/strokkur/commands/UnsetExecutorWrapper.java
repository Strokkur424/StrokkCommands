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

/// Unsets an inherited executor wrapper. This annotation can be used if an entire class is annotated
/// with a [CustomExecutorWrapper]-annotated annotation, but certain children should be excluded from this.
///
/// ## Example usage
/// ```java
/// @TimingsExecutor
/// @Command("heavy-command")
/// class HeavyCommand {
///
///   @Executes
///   void someHeavyOperation() {
///     // This is wrapped by `@TimingsExecutor`
///   }
///
///   @Executes("light")
///   @UnsetExecutorWrapper
///   void someLightOperation() {
///     // This handler is *not* wrapped by `@TimingsExecutor`
///   }
/// }
/// ```
///
/// @see CustomExecutorWrapper
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface UnsetExecutorWrapper {
}
