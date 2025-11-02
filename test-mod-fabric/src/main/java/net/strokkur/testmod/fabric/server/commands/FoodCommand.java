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
package net.strokkur.testmod.fabric.server.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.strokkur.commands.Command;
import net.strokkur.commands.CustomSuggestion;
import net.strokkur.commands.Executes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Command("food")
public class FoodCommand {
  private final static List<String> validInput = List.of(
      "peanuts", "oreos", "apples", "cats"
  );

  @Executes("select")
  void sender(CommandSourceStack source, @WordSuggestions String word) {
    if (validInput.contains(word)) {
      source.sendSuccess(() -> Component.literal(word + " is very tasty!"), true);
      return;
    }

    source.sendFailure(Component.literal(word + " is not a valid selection."));
  }

  @WordSuggestions
  static CompletableFuture<Suggestions> suggest(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
    validInput.stream()
        .filter(str -> str.startsWith(builder.getRemainingLowerCase()))
        .forEach(builder::suggest);
    return builder.buildFuture();
  }

  @CustomSuggestion
  private @interface WordSuggestions {}
}
