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
import net.strokkur.jap.code.expression.builder.MethodInvocationBuilder;
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
      final boolean optional = isOptional(parameter.type().toType());
      final ConvertToExpression retriever = optional ?
      ((CodeClassType) typeToCheck(parameter.type().toType()))
        .withoutGenerics()
        .chainField("class") :
        ((CodeClassType) parameter.type().toType())
          .withoutGenerics()
          .chainField("class");
      return new BrigadierArgumentType(
        "custom-arg-" + customArg.type(),
        Expressions.variable("custom_arg_" + classType.simpleName()),
        Expressions.variable("ctx")
          .chainMethod("getArgument", Expressions.string(argumentName), retriever),
        () -> Expressions.lambdaInline("(ctx, b)",
          Expressions.variable("custom_arg_" + classType.simpleName())
            .chainMethod("listSuggestions",
              Expressions.variable("ctx"),
              Expressions.variable("b"))),
        optional);
    }

    boolean optional = false;
    CodeType checkOn = parameter.type().toType();
    if (isOptional(checkOn)) {
      optional = true;
      checkOn = typeToCheck(checkOn);
    }
    if (checkOn.isType(VelocityClasses.PLAYER)) {
      return new BrigadierArgumentType(
        "player-arg-target",
        Classes.STRING_ARGUMENT_TYPE
          .chainMethod("word"),
        this.retrieve("getPlayer", argumentName, "player"),
        this.buildSuggestionProvider(
          Expressions.variable("server")
            .chainMethod("getAllPlayers"),
          Expressions.variable("suggestion")
            .chainMethod("getUsername")
        ),
        optional
      );
    }

    if (checkOn.isType(VelocityClasses.REGISTERED_SERVER)) {
      return new BrigadierArgumentType(
        "server-arg-target",
        Classes.STRING_ARGUMENT_TYPE
          .chainMethod("word"),
        this.retrieve("getServer", argumentName, "server"),
        this.buildSuggestionProvider(
          Expressions.variable("server")
            .chainMethod("getAllServers"),
          Expressions.variable("suggestion")
            .chainMethod("getServerInfo")
            .chainMethod("getName")
        ),
        optional
      );
    }

    return null;
  }

  private ConvertToExpression retrieve(
    String getMethod,
    String argumentName,
    String invalidWhat
  ) {
    return Expressions.variable("server")
      .chainMethod(getMethod,
        Expressions.variable("ctx")
          .chainMethod("getArgument",
            Expressions.string(argumentName), JavaTypes.STRING.withoutGenerics().chainField("class")))
      .chainMethod("orElseThrow",
        Expressions.lambdaInline(
          Classes.SIMPLE_COMMAND_EXCEPTION_TYPE
            .ctor(Classes.LITERAL_MESSAGE.ctor(
              Expressions.string("Invalid " + invalidWhat + " provided!")
            )).chainMethod("create"))).setStyle(StyleConfig.MULTILINE);
  }

  private SuggestionProvider buildSuggestionProvider(
    MethodInvocationBuilder filterStart,
    MethodInvocationBuilder suggest
  ) {
    return () -> Expressions.lambda("(ctx, b)",
      filterStart
        .chainMethod("stream")
        .chainMethod("filter",
          Expressions.lambdaInline("(suggestion)",
            suggest
              .chainMethod("toLowerCase")
              .chainMethod("startsWith", Expressions.variable("b")
                .chainMethod("getRemainingLowerCase")))).setStyle(StyleConfig.MULTILINE)
        .chainMethod("forEach", Expressions.lambdaInline("(suggestion)",
          Expressions.variable("b")
            .chainMethod("suggest", suggest))),
      Statements.returnStmt(
        Expressions.variable("b")
          .chainMethod("buildFuture")
      ));
  }
}
