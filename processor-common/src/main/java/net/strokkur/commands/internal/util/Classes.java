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
package net.strokkur.commands.internal.util;

public interface Classes {
  // Java types
  String LIST = "java.util.List";
  String COLLECTIONS = "java.util.Collections";
  String ARRAYS = "java.util.Arrays";
  String LIST_STRING = LIST + "<java.lang.String>";
  String PREDICATE = "java.util.function.Predicate";
  String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";
  String METHOD = "java.lang.reflect.Method";

  // Brigadier types
  String COMMAND = "com.mojang.brigadier.Command";
  String COMMAND_DISPATCHER = "com.mojang.brigadier.CommandDispatcher";
  String LITERAL_COMMAND_NODE = "com.mojang.brigadier.tree.LiteralCommandNode";
  String LITERAL_ARGUMENT_BUILDER = "com.mojang.brigadier.builder.LiteralArgumentBuilder";
  String SIMPLE_COMMAND_EXCEPTION_TYPE = "com.mojang.brigadier.exceptions.SimpleCommandExceptionType";
  String LITERAL_MESSAGE = "com.mojang.brigadier.LiteralMessage";
  String COMMAND_CONTEXT = "com.mojang.brigadier.context.CommandContext";
  String SUGGESTIONS = "com.mojang.brigadier.suggestion.Suggestions";
  String SUGGESTION_PROVIDER = "com.mojang.brigadier.suggestion.SuggestionProvider";
  String SUGGESTIONS_BUILDER = "com.mojang.brigadier.suggestion.SuggestionsBuilder";

  // Other
  String NULL_MARKED = "org.jspecify.annotations.NullMarked";
}
