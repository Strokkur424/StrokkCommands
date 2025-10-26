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
