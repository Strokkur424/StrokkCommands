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
package net.strokkur.commands.internal.arguments;

import net.strokkur.commands.internal.intermediate.registrable.EnumSuggestionProvider;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.documentation.DiscardingDocumentationRenderer;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.visitor.source.JavaSourcePrintingVisitor;
import org.jspecify.annotations.Nullable;

public record BrigadierArgumentType(
  String argumentTypeIdentifier,
  ConvertToExpression initializer, ConvertToExpression retriever,
  @Nullable SuggestionProvider defaultSuggestionProvider, boolean isOptional
) {

  public static BrigadierArgumentType of(
    String argumentTypeIdentifier,
    ConvertToExpression initializer, ConvertToExpression retriever
  ) {
    return of(argumentTypeIdentifier, initializer, retriever, false);
  }

  public static BrigadierArgumentType of(
    String argumentTypeIdentifier,
    ConvertToExpression initializer, ConvertToExpression retriever,
    boolean optional
  ) {
    return new BrigadierArgumentType(argumentTypeIdentifier, initializer, retriever, null, optional);
  }

  public static BrigadierArgumentType ofEnum(
    String argumentTypeIdentifier,
    CodeClassType type, boolean optional
  ) {
    final EnumSuggestionProvider provider =new EnumSuggestionProvider(type);
    return new BrigadierArgumentType(
      argumentTypeIdentifier,
      Classes.STRING_ARGUMENT_TYPE.chainMethod("word"),
      provider.createRetrieveExpr(argumentTypeIdentifier),
      provider,
      optional
    );
  }

  public BrigadierArgumentType withOptional(boolean optional) {
    return new BrigadierArgumentType(argumentTypeIdentifier, initializer, retriever, defaultSuggestionProvider, optional);
  }

  @Override
  public String toString() {
    final JavaSourcePrintingVisitor visitor = new JavaSourcePrintingVisitor(DiscardingDocumentationRenderer::new, "", "");
    final String rawInitializer = initializer().toExpression().accept(visitor).toString();
    final String rawRetriever = retriever().toExpression().accept(visitor).toString();
    final String initializer = rawInitializer.replace("\n", " ");
    final String retriever = rawRetriever.replace("\n", " ");

    return "[" + initializer + ", " + retriever + "]";
  }
}
