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
package net.strokkur.commands.internal.intermediate.registrable;

import net.strokkur.commands.internal.util.Classes;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToStatement;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.statement.Statements;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.type.CodeTypes;
import net.strokkur.jap.code.type.preset.JavaTypes;

import java.util.Map;

public record EnumSuggestionProvider(
  CodeClassType enumType
) implements SuggestionProvider {

  private String variableName() {
    return Character.toLowerCase(enumType.name().charAt(0)) + enumType.name().substring(1) + "Types";
  }

  public ConvertToStatement createVariableStmt() {
    return Statements.variableDeclarationFinal(
      CodeTypes.ofJavaClass(Map.class).typed(JavaTypes.STRING, enumType),
      variableName(),
      Expressions.methodInvocation("createEnumValuesMap").addParameters(enumType.chainMethod("values"))
    );
  }

  @Override
  public ConvertToExpression suggestionExpression() {
    return Expressions.methodInvocation("getEnumSuggestions").addParameters(Expressions.variable(variableName()));
  }

  public ConvertToExpression createRetrieveExpr(String argName) {
    final ConvertToExpression base = Classes.STRING_ARGUMENT_TYPE.chainMethod(
      "getString",
      Expressions.variable("ctx"),
      Expressions.string(argName)
    );

    return Expressions.methodInvocation("getEnumValue")
      .addParameters(
        Expressions.variable(variableName()),
        base
      );
  }
}
