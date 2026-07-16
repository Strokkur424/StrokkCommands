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
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceField;
import net.strokkur.jap.source.classmodel.SourceMethod;
import net.strokkur.jap.source.classmodel.SourceMethodParameter;

import java.util.List;

public class SuggestionsRegistry extends FunctionalInterfaceRegistry<SuggestionProvider> {

  public SuggestionsRegistry(CodeClassType platformType) {
    super(platformType);
  }

  @Override
  protected boolean inlineMethodPredicate(SourceMethod source) {
    final List<SourceMethodParameter> params = source.parameters();
    return JavaTypes.COMPLETABLE_FUTURE.typed(Classes.SUGGESTIONS).equals(source.returnType().toType())
      && params.size() == 2
      && Classes.COMMAND_CONTEXT.typed(getPlatformType()).equals(params.getFirst().type().toType())
      && Classes.SUGGESTIONS_BUILDER.equals(params.get(1).type().toType());
  }

  @Override
  protected boolean providerMethodPredicate(SourceMethod source) {
    return Classes.SUGGESTION_PROVIDER.typed(getPlatformType()).equals(source.returnType().toType())
      && source.parameters().isEmpty();
  }

  @Override
  protected boolean instancePredicate(SourceClass source) {
    return source.implementsClasses()
      .stream().anyMatch(impl -> Classes.SUGGESTION_PROVIDER.typed(getPlatformType()).equals(impl.classType()));
  }

  @Override
  protected boolean fieldPredicate(SourceField source) {
    return Classes.SUGGESTION_PROVIDER.typed(getPlatformType()).equals(source.type().toType());
  }

  @Override
  protected SuggestionProvider createInline(net.strokkur.jap.source.classmodel.SourceMethod method) {
    return new MethodImpl(method, true);
  }

  @Override
  protected SuggestionProvider createProvider(net.strokkur.jap.source.classmodel.SourceMethod method) {
    return new MethodImpl(method, false);
  }

  @Override
  protected SuggestionProvider createField(net.strokkur.jap.source.classmodel.SourceField field) {
    return new FieldImpl(field);
  }

  @Override
  protected SuggestionProvider createInstance(net.strokkur.jap.source.classmodel.SourceClass source) {
    return new InstanceImpl(source);
  }
}
