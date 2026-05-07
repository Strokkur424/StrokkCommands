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

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceField;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.util.Classes;

import java.util.List;

public class SuggestionsRegistry extends FunctionalInterfaceRegistry<SuggestionProvider> {

  public SuggestionsRegistry(final String platformType) {
    super(platformType);
  }

  @Override
  protected boolean inlineMethodPredicate(final SourceMethod source) {
    final List<SourceParameter> params = source.getParameters();
    return source.getReturnType().getFullyQualifiedAndTypedName().equals(Classes.COMPLETABLE_FUTURE + "<" + Classes.SUGGESTIONS + ">")
        && params.size() == 2
        && params.getFirst().getType().getFullyQualifiedAndTypedName().equals(Classes.COMMAND_CONTEXT + "<" + getPlatformType() + ">")
        && params.get(1).getType().getFullyQualifiedName().equals(Classes.SUGGESTIONS_BUILDER);
  }

  @Override
  protected boolean providerMethodPredicate(final SourceMethod source) {
    return source.getReturnType().getFullyQualifiedAndTypedName().equals(Classes.SUGGESTION_PROVIDER + "<" + getPlatformType() + ">")
        && source.getParameters().isEmpty();
  }

  @Override
  protected boolean instancePredicate(final SourceClass source) {
    return source.implementsInterface(Classes.SUGGESTION_PROVIDER + "<" + getPlatformType() + ">");
  }

  @Override
  protected boolean fieldPredicate(final SourceField source) {
    return source.getType().getFullyQualifiedAndTypedName().equals(Classes.SUGGESTION_PROVIDER + "<" + getPlatformType() + ">");
  }

  @Override
  protected SuggestionProvider createInline(final SourceClass enclosed, final SourceMethod method) {
    return new MethodImpl(enclosed, method, true);
  }

  @Override
  protected SuggestionProvider createProvider(final SourceClass enclosed, final SourceMethod method) {
    return new MethodImpl(enclosed, method, false);
  }

  @Override
  protected SuggestionProvider createField(final SourceClass enclosed, final SourceField field) {
    return new FieldImpl(enclosed, field);
  }

  @Override
  protected SuggestionProvider createInstance(final SourceClass source) {
    return new InstanceImpl(source);
  }
}
