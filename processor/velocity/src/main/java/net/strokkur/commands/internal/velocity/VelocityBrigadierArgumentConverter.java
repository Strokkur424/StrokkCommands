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
package net.strokkur.commands.internal.velocity;

import com.google.auto.service.AutoService;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.velocity.arguments.CustomArg;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.statement.Statements;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.type.CodeType;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.code.util.StyleConfig;
import net.strokkur.jap.source.annotation.SourceAnnotation;
import net.strokkur.jap.source.classmodel.SourceParameterLike;
import org.jspecify.annotations.Nullable;

@AutoService(BrigadierArgumentConverter.class)
public final class VelocityBrigadierArgumentConverter extends BrigadierArgumentConverter {

  @Override
  protected @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(
    String argumentName, CodeType type, SourceParameterLike parameter) {
    if (parameter.hasAnnotation(CustomArg.class)) {
      final SourceAnnotation customArg = parameter.getAnnotation(CustomArg.class);
      final CodeClassType classType = customArg.parameter("value").classValue();
      return new BrigadierArgumentType(
        "custom-arg-" + customArg.type(),
        classType.ctor(),
        Expressions.variable("ctx")
          .chainMethod(
            "getArgument",
            Expressions.string(argumentName),
            ((CodeClassType) parameter.type().toType())
              .withoutGenerics()
              .chainField("class")),
        () -> Expressions.lambdaInline("(ctx, b)",
          classType.ctor()
            .chainMethod("listSuggestions",
              Expressions.variable("ctx"),
              Expressions.variable("b"))), false);
    }

    if (parameter.type().isType(VelocityClasses.PLAYER)) {
      return new BrigadierArgumentType(
        "player-arg-target",
        Classes.STRING_ARGUMENT_TYPE
          .chainMethod("word"),
        this.retrievePlayer(argumentName),
        this.playerSuggestionProvider(),
        false
      );
    }

    return null;
  }

  private ConvertToExpression retrievePlayer(String argumentName) {
    return Expressions.variable("server")
      .chainMethod("getPlayer",
        Expressions.variable("ctx")
          .chainMethod("getArgument",
            Expressions.string(argumentName), JavaTypes.STRING.withoutGenerics().chainField("class")))
      .chainMethod("orElseThrow",
        Expressions.lambdaInline(
          Classes.SIMPLE_COMMAND_EXCEPTION_TYPE
            .ctor(Classes.LITERAL_MESSAGE.ctor(
              Expressions.string("Invalid player provided")
            )).chainMethod("create"))).setStyle(StyleConfig.MULTILINE);
  }

  private SuggestionProvider playerSuggestionProvider() {
    return () -> Expressions.lambda("(ctx, b)",
      Expressions.variable("server")
        .chainMethod("getAllPlayers")
        .chainMethod("stream")
        .chainMethod("filter",
          Expressions.lambdaInline("(p)",
            Expressions.variable("p")
              .chainMethod("getUsername")
              .chainMethod("toLowerCase")
              .chainMethod("startsWith", Expressions.variable("b")
                .chainMethod("getRemainingLowerCase")))).setStyle(StyleConfig.MULTILINE)
        .chainMethod("forEach", Expressions.lambdaInline("(suggestion)",
          Expressions.variable("b")
            .chainMethod("suggest",
              Expressions.variable("suggestion")
                .chainMethod("getUsername")))),
      Statements.returnStmt(
        Expressions.variable("b")
          .chainMethod("buildFuture")
      ));
  }
}
