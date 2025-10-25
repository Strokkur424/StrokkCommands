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

