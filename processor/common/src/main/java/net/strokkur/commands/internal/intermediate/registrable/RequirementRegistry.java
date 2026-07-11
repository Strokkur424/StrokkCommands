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

import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceField;
import net.strokkur.jap.source.classmodel.SourceMethod;
import net.strokkur.jap.source.classmodel.SourceMethodParameter;
import net.strokkur.jap.source.type.SourcePrimitiveType;

import java.util.List;

public class RequirementRegistry extends FunctionalInterfaceRegistry<RequirementProvider> {

  public RequirementRegistry(CodeClassType platformType) {
    super(platformType);
  }

  @Override
  protected boolean inlineMethodPredicate(SourceMethod source) {
    final List<SourceMethodParameter> params = source.parameters();
    return SourcePrimitiveType.BOOL.equals(source.returnType())
      && params.size() == 1
      && getPlatformType().equals(params.getFirst().type().toType());
  }

  @Override
  protected boolean providerMethodPredicate(SourceMethod source) {
    return JavaTypes.PREDICATE.typed(getPlatformType()).equals(source.returnType().toType());
  }

  @Override
  protected boolean instancePredicate(SourceClass source) {
    return source.implementsClasses().stream()
      .anyMatch(type -> JavaTypes.PREDICATE.typed(getPlatformType()).equals(type.classType()));
  }

  @Override
  protected boolean fieldPredicate(SourceField source) {
    return JavaTypes.PREDICATE.typed(getPlatformType()).equals(source.type().toType());
  }

  @Override
  protected RequirementProvider createInline(SourceMethod method) {
    return new MethodImpl(method, true);
  }

  @Override
  protected RequirementProvider createProvider(SourceMethod method) {
    return new MethodImpl(method, false);
  }

  @Override
  protected RequirementProvider createField(SourceField field) {
    return new FieldImpl(field);
  }

  @Override
  protected RequirementProvider createInstance(SourceClass source) {
    return new InstanceImpl(source);
  }
}
