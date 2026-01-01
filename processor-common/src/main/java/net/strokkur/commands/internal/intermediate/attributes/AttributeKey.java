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

import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public interface AttributeKey<T> {
  AttributeKey<Parameterizable> RECORD_ARGUMENTS = create("record_arguments", null);
  AttributeKey<Executable> EXECUTABLE = create("executable", null);
  AttributeKey<DefaultExecutable> DEFAULT_EXECUTABLE = create("default_executable", null);
  AttributeKey<ExecutorWrapperProvider> EXECUTOR_WRAPPER = create("executor_wrapper", null);
  AttributeKey<List<ExecuteAccess<?>>> ACCESS_STACK = create("access_stack", null);

  AttributeKey<RequirementProvider> REQUIREMENT_PROVIDER = create("requirement_provider", null);
  AttributeKey<SuggestionProvider> SUGGESTION_PROVIDER = create("suggestion_provider", null);

  static <T> AttributeKey<T> create(String key, @Nullable T defaultValue) {
    return new StaticAttributeKey<>(key, defaultValue);
  }

  static <T> AttributeKey<T> createDynamic(String key, Supplier<@Nullable T> defaultValue) {
    return new DynamicAttributeKey<>(key, defaultValue);
  }

  @Contract(pure = true)
  String key();

  @Nullable
  @Contract(pure = true)
  T defaultValue();
}
