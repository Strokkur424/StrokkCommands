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
package net.strokkur.testplugin.velocity.iceacream.subpackage;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import net.kyori.adventure.text.Component;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class IceCreamArgument implements ArgumentType<IceCreamType> {

  private static final List<IceCreamType> TYPES = List.of(IceCreamType.values());

  private static final DynamicCommandExceptionType NOT_ICE_CREAM =
    new DynamicCommandExceptionType(obj ->
      VelocityBrigadierMessage.tooltip(Component.text(obj + " is not a valid ice cream!")));

  @Override
  public IceCreamType parse(StringReader reader) throws CommandSyntaxException {
    final String read = reader.readUnquotedString(); // StringArgumentType.word
    try {
      return IceCreamType.valueOf(read.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw NOT_ICE_CREAM.create(read);
    }
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
    CommandContext<S> context, SuggestionsBuilder builder) {
    TYPES.stream()
      .map(Object::toString)
      .filter(name -> name.startsWith(builder.getRemainingLowerCase()))
      .forEach(builder::suggest);
    return builder.buildFuture();
  }
}
