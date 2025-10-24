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
package net.strokkur.testplugin.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.strokkur.commands.annotations.Suggestion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
class CommandWithSuggestions {

  static CompletableFuture<Suggestions> mySuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
    builder.suggest("abc");
    builder.suggest("xyz");
    return builder.buildFuture();
  }

  static SuggestionProvider<CommandSourceStack> mySuggestions() {
    return (ctx, builder) -> {
      Bukkit.getOnlinePlayers().stream()
          .map(Player::getName)
          .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
          .forEach(builder::suggest);
      return builder.buildFuture();
    };
  }

  void executesField(CommandSender sender, @Suggestion(base = SuggestionsRepository.class, field = "STATIC_FIELD") String value) {
    // ...
  }

  void executesMethodRef(CommandSender sender, @Suggestion(method = "mySuggestions") String value) {
    // ...
  }

  void executesMethod(CommandSender sender, @Suggestion(method = "mySuggestions", reference = false) String value) {
    // ...
  }

  void executesClass(CommandSender sender, @Suggestion(base = SuggestionsRepository.SomeClass.class) int value) {
    // ...
  }
}
