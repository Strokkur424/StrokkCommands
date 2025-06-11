package net.strokkur.testplugin.iceacream;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class IceCreamArgument implements CustomArgumentType.Converted<IceCreamType, String> {

    private static final List<IceCreamType> TYPES = List.of(IceCreamType.values());

    private static final DynamicCommandExceptionType NOT_ICE_CREAM = new DynamicCommandExceptionType(
        obj -> MessageComponentSerializer.message().serialize(Component.text(obj + " is not a valid ice cream!"))
    );

    @Override
    public IceCreamType convert(String nativeType) throws CommandSyntaxException {
        try {
            return IceCreamType.valueOf(nativeType.toUpperCase());
        } catch (IllegalArgumentException notIceCream) {
            throw NOT_ICE_CREAM.create(nativeType);
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        TYPES.stream()
            .map(Object::toString)
            .filter(name -> name.startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
