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

import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.as.AsCodeType;

import java.util.Arrays;

public enum Classes implements AsCodeType<CodeType.ClassType> {
  // Java types
  STRING("java.lang.String"),
  BOOLEAN("java.lang.Boolean"),

  LIST("java.util.List"),
  COLLECTIONS("java.util.Collections"),
  ARRAYS("java.util.Arrays"),

  PREDICATE("java.util.function.Predicate"),
  COMPLETABLE_FUTURE("java.util.concurrent.CompletableFuture"),

  METHOD("java.lang.reflect.Method"),
  ILLEGAL_ACCESS_EXCEPTION("java.lang.IllegalAccessException"),

  LIST_STRING(LIST, STRING),

  // Brigadier types
  COMMAND("com.mojang.brigadier.Command"),
  COMMAND_DISPATCHER("com.mojang.brigadier.CommandDispatcher"),
  LITERAL_COMMAND_NODE("com.mojang.brigadier.tree.LiteralCommandNode"),
  LITERAL_ARGUMENT_BUILDER("com.mojang.brigadier.builder.LiteralArgumentBuilder"),
  SIMPLE_COMMAND_EXCEPTION_TYPE("com.mojang.brigadier.exceptions.SimpleCommandExceptionType"),
  LITERAL_MESSAGE("com.mojang.brigadier.LiteralMessage"),
  COMMAND_CONTEXT("com.mojang.brigadier.context.CommandContext"),
  SUGGESTIONS("com.mojang.brigadier.suggestion.Suggestions"),
  SUGGESTION_PROVIDER("com.mojang.brigadier.suggestion.SuggestionProvider"),
  SUGGESTIONS_BUILDER("com.mojang.brigadier.suggestion.SuggestionsBuilder"),

  BOOL_ARGUMENT_TYPE("com.mojang.brigadier.arguments.BoolArgumentType"),
  INTEGER_ARGUMENT_TYPE("com.mojang.brigadier.arguments.IntegerArgumentType"),
  LONG_ARGUMENT_TYPE("com.mojang.brigadier.arguments.LongArgumentType"),
  FLOAT_ARGUMENT_TYPE("com.mojang.brigadier.arguments.FloatArgumentType"),
  DOUBLE_ARGUMENT_TYPE("com.mojang.brigadier.arguments.DoubleArgumentType"),
  STRING_ARGUMENT_TYPE("com.mojang.brigadier.arguments.StringArgumentType"),

  // Other
  NULL_MARKED("org.jspecify.annotations.NullMarked"),
  NULLABLE("org.jspecify.annotations.Nullable"),
  INJECT("jakarta.inject.Inject");

  private final CodeType.ClassType classType;

  Classes(String fqn) {
    this.classType = CodeType.ofClass(fqn);
  }

  Classes(Classes base, Classes... types) {
    this.classType = CodeType.ofClassTyped(base.classType.codeClass(), Arrays.stream(types)
        .map(Classes::getAsCodeType)
        .toArray(CodeType[]::new)
    );
  }

  @Override
  public CodeType.ClassType getAsCodeType() {
    return classType;
  }
}
