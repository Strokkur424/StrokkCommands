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
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
interface SuggestionsRepository {

    SuggestionProvider<CommandSourceStack> STATIC_FIELD = (ctx, builder) -> {
        builder.suggest(8);
        builder.suggest(16);
        builder.suggest(32);
        return builder.buildFuture();
    };

    class SomeClass implements SuggestionProvider<CommandSourceStack> {
        private final List<String> suggestions;

        public SomeClass() {
            suggestions = new ArrayList<>(64);
            for (int i = 1; i <= 64; i++) {
                suggestions.add(Integer.toString(i));
            }
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
            suggestions.stream()
                .filter(num -> num.startsWith(builder.getRemaining()))
                .forEach(builder::suggest);
            return builder.buildFuture();
        }
    }
}
