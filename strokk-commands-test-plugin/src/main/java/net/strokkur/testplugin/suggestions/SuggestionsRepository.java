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
