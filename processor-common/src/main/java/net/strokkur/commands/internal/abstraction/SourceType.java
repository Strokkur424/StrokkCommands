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
package net.strokkur.commands.internal.abstraction;

import javax.lang.model.element.Modifier;
import java.util.Set;

/// Represents a type in the Java source code.
///
/// This can be a [VoidSourceType] for `void`, a [SourcePrimitive] for any form of
/// primitive (a.e.: `int`), or [SourceClass], for any class.
///
/// The type represented here might also be an array. You can get the type of the
/// array by casting to [SourceArray] and calling [SourceArray#getArrayType()]. Note that
/// this type might also be an array (if the original type was a multi-dimensional array).
public interface SourceType extends SourceElement {
  /// The fully qualified name.
  ///
  /// A class `my.package.Class$Inner` would return `my.package.Class.Inner`.
  ///
  /// @return the fully qualified name
  String getFullyQualifiedName();

  /// The package name of this class.
  ///
  /// A class `my.package.Class$Inner` would return `my.package`.
  ///
  /// @return the package
  String getPackageName();

  /// A name suitable for representing this type in source code.
  ///
  /// A class `my.package.Class$Inner` would return `Class.Inner`.
  ///
  /// @return the source name
  String getSourceName();

  /// The simplest name of this source type.
  ///
  /// A class `my.package.Class$Inner` would return `Inner`.
  ///
  /// @return the simple name of this type
  String getName();

  /// {@return whether this type is an array}
  boolean isArray();

  /// {@return the modifiers of this source type}
  Set<Modifier> getModifiers();

  /// {@return the imports needed for this type}
  Set<String> getImports();
}
