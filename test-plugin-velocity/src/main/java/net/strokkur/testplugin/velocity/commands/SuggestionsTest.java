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
package net.strokkur.testplugin.velocity.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.CommandSource;
import net.strokkur.commands.Command;
import net.strokkur.commands.CustomSuggestion;
import net.strokkur.commands.Executes;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Command("with-suggestions")
class SuggestionsTest {

  @Executes
  void execute(CommandSource source, @Suggestion String message) {
    source.sendRichMessage("You entered: <aqua>" + message);
  }

  @Suggestion
  static CompletableFuture<Suggestions> suggest(final CommandContext<CommandSource> ctx, final SuggestionsBuilder builder) {
    Stream.of("one", "two", "three", "four", "five", "six", "seven")
        .filter(it -> it.startsWith(builder.getRemainingLowerCase()))
        .forEach(builder::suggest);
    return builder.buildFuture();
  }

  @CustomSuggestion
  @interface Suggestion {}
}

