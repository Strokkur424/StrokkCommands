package net.strokkur.testplugin.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.SuggestionClass;
import net.strokkur.commands.annotations.SuggestionField;
import net.strokkur.commands.annotations.SuggestionMethod;
import net.strokkur.commands.annotations.arguments.IntArg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@Command("withsuggestions")
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

    @Executes("field")
    void executesField(CommandSender sender, @SuggestionField(base = SuggestionsRepository.class, field = "STATIC_FIELD") String value) {
        // ...
    }

    @Executes("methodRef")
    void executesMethodRef(CommandSender sender, @SuggestionMethod(method = "mySuggestions") String value) {
        // ...
    }

    @Executes("method")
    void executesMethod(CommandSender sender, @SuggestionMethod(method = "mySuggestions", reference = false) String value) {
        // ...
    }

    @Executes("class")
    void executesClass(CommandSender sender, @SuggestionClass(SuggestionsRepository.SomeClass.class) @IntArg(min = 1, max = 64) int value) {
        // ...
    }
}