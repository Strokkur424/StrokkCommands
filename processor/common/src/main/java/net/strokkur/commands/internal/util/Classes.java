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

import net.strokkur.jap.code.convert.ConvertToClassType;
import net.strokkur.jap.code.type.CodeTypes;

public interface Classes extends ConvertToClassType {
  // Brigadier types
  Classes COMMAND = create("com.mojang.brigadier.Command");
  Classes COMMAND_DISPATCHER = create("com.mojang.brigadier.CommandDispatcher");
  Classes LITERAL_COMMAND_NODE = create("com.mojang.brigadier.tree.LiteralCommandNode");
  Classes LITERAL_ARGUMENT_BUILDER = create("com.mojang.brigadier.builder.LiteralArgumentBuilder");
  Classes SIMPLE_COMMAND_EXCEPTION_TYPE = create("com.mojang.brigadier.exceptions.SimpleCommandExceptionType");
  Classes COMMAND_SYNTAX_EXCEPTION = create("com.mojang.brigadier.exceptions.CommandSyntaxException");
  Classes LITERAL_MESSAGE = create("com.mojang.brigadier.LiteralMessage");
  Classes COMMAND_CONTEXT = create("com.mojang.brigadier.context.CommandContext");
  Classes SUGGESTIONS = create("com.mojang.brigadier.suggestion.Suggestions");
  Classes SUGGESTION_PROVIDER = create("com.mojang.brigadier.suggestion.SuggestionProvider");
  Classes SUGGESTIONS_BUILDER = create("com.mojang.brigadier.suggestion.SuggestionsBuilder");

  Classes BOOL_ARGUMENT_TYPE = create("com.mojang.brigadier.arguments.BoolArgumentType");
  Classes INTEGER_ARGUMENT_TYPE = create("com.mojang.brigadier.arguments.IntegerArgumentType");
  Classes LONG_ARGUMENT_TYPE = create("com.mojang.brigadier.arguments.LongArgumentType");
  Classes FLOAT_ARGUMENT_TYPE = create("com.mojang.brigadier.arguments.FloatArgumentType");
  Classes DOUBLE_ARGUMENT_TYPE = create("com.mojang.brigadier.arguments.DoubleArgumentType");
  Classes STRING_ARGUMENT_TYPE = create("com.mojang.brigadier.arguments.StringArgumentType");

  // Other
  Classes NULL_MARKED = create("org.jspecify.annotations.NullMarked");
  Classes NULLABLE = create("org.jspecify.annotations.Nullable");

  static Classes create(String fqn) {
    return () -> CodeTypes.ofClass(fqn);
  }

  static Classes ofClass(Class<?> classType) {
    return () -> CodeTypes.ofClass(classType.getName());
  }
}
