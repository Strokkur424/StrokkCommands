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
package net.strokkur.commands.internal.paper;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgumentImpl;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.suggestions.SuggestionProvider;
import net.strokkur.commands.internal.paper.util.PaperAttributeKeys;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.paper.Permission;
import net.strokkur.commands.paper.RequiresOP;
import net.strokkur.commands.paper.Suggestion;
import org.jspecify.annotations.Nullable;

import java.util.Set;

final class PaperPlatformUtils extends PlatformUtils {
  public PaperPlatformUtils(final MessagerWrapper messager, final BrigadierArgumentConverter converter) {
    super(messager, converter);
  }

  @Override
  protected RequiredCommandArgument constructRequiredCommandArgument(final BrigadierArgumentType type, final String name, final SourceVariable parameter, final SourceClass source) {
    final RequiredCommandArgument out = new RequiredCommandArgumentImpl(type, name, parameter);

    final SuggestionProvider suggestionProvider = getSuggestionProvider(source, parameter);
    if (suggestionProvider != null) {
      debug("  | Suggestion provider: {}", suggestionProvider);
      out.setAttribute(PaperAttributeKeys.SUGGESTION_PROVIDER, suggestionProvider);
    }

    return out;
  }

  @Override
  public void populateNode(final CommandNode node, final AnnotationsHolder element) {
    element.getAnnotationOptional(Permission.class).ifPresent(
        permission -> node.editAttributeMutable(PaperAttributeKeys.PERMISSIONS, s -> s.add(permission.value()), () -> Set.of(permission.value()))
    );

    if (element.hasAnnotation(RequiresOP.class)) {
      node.setAttribute(PaperAttributeKeys.REQUIRES_OP, true);
    }
  }

  @Nullable
  private SuggestionProvider getSuggestionProvider(final SourceClass classElement, final SourceVariable parameter) {
    final Suggestion suggestion = parameter.getAnnotation(Suggestion.class);
    if (suggestion == null) {
      return null;
    }

    final SourceClass base = parameter.getAnnotationSourceClassField(Suggestion.class, "base");
    final SourceClass baseClass = base == null ? classElement : base;

    if (suggestion.method().isBlank() && suggestion.field().isBlank()) {
      if (base == null) {
        infoElement("@Suggestion annotation was used, but no parameters were passed.", parameter);
        return null;
      }

      return SuggestionProvider.ofClass(baseClass);
    }

    if (!suggestion.method().isBlank()) {
      if (suggestion.reference()) {
        return SuggestionProvider.ofMethodReference(baseClass, suggestion.method());
      }
      return SuggestionProvider.ofMethod(baseClass, suggestion.method());
    }

    if (!suggestion.field().isBlank()) {
      return SuggestionProvider.ofField(baseClass, suggestion.field());
    }

    errorSource("Internal exception: Suggestion annotation is not null, but no provider was found. Please report this at https://discord.strokkur.net.", parameter);
    return null;
  }
}
