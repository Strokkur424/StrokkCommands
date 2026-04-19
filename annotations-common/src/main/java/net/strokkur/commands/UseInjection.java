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

/// Declares that the Brigadier source file should be generated with dependency injection frameworks
/// in mind. This includes the following changes versus typical generated source files:
///
/// - The create and register methods are no longer static, instead generating as instance methods.
/// - Any class instances, which would've been constructed manually instead get added as uninstantiated fields
///   which a javax `@Inject` annotation added.
/// - Constructor parameters are no longer propagated to the create/register methods.
///
/// @since 2.1.0
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
public @interface UseInjection {
}
