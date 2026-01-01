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
package net.strokkur.commands.internal.intermediate.attributes;

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceMethod;

/**
 * Holds information about an executor wrapper.
 *
 * @param wrapperMethod        The method that implements the wrapper
 * @param wrapperAnnotationFqn The fully qualified name of the wrapper annotation (e.g., "com.example.TimingWrapper")
 * @param isStatic             Whether the wrapper method is static
 * @param wrapperType          The type of wrapper (Command return vs int return)
 */
public record ExecutorWrapperProvider(
    SourceMethod wrapperMethod,
    String wrapperAnnotationFqn,
    boolean isStatic,
    WrapperType wrapperType
) {

  /**
   * The type of wrapper based on the method signature.
   */
  public enum WrapperType {
    /**
     * Returns Command<S>, takes (Command<S> executor, Method method)
     * Full wrapper that wraps the executor and returns a new Command.
     */
    COMMAND_WRAPPER,

    /**
     * Returns int, takes (CommandContext<S> ctx, Command<S> executor, Method method)
     * Direct execution that runs and returns the result.
     */
    INT_EXECUTOR,

    /**
     * Returns void, takes (CommandContext<S> ctx, Command<S> executor, Method method)
     * The method runs the executor internally and can do pre/post processing.
     */
    VOID_EXECUTOR
  }

  /**
   * Gets the class that contains the wrapper method.
   */
  public SourceClass wrapperClass() {
    return wrapperMethod.getEnclosed();
  }
}
