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
import net.strokkur.commands.internal.abstraction.SourcePrimitive;
import net.strokkur.commands.internal.util.Classes;

import java.util.List;

public class RequirementRegistry extends FunctionalInterfaceRegistry<RequirementProvider> {

  public RequirementRegistry(final String platformType) {
    super(platformType);
  }

  @Override
  protected boolean inlineMethodPredicate(final SourceMethod source) {
    final List<SourceParameter> params = source.getParameters();
    return source.getReturnType() instanceof SourcePrimitive primitive
        && primitive.getName().equals("boolean")
        && params.size() == 1
        && params.getFirst().getType().getFullyQualifiedName().equals(getPlatformType());
  }

  @Override
  protected boolean providerMethodPredicate(final SourceMethod source) {
    return source.getReturnType().getFullyQualifiedAndTypedName().equals(Classes.PREDICATE + "<" + getPlatformType() + ">");
  }

  @Override
  protected boolean instancePredicate(final SourceClass source) {
    return source.implementsInterface(Classes.PREDICATE + "<" + getPlatformType() + ">");
  }

  @Override
  protected boolean fieldPredicate(final SourceField source) {
    return source.getType().getFullyQualifiedAndTypedName().equals(Classes.PREDICATE + "<" + getPlatformType() + ">");
  }

  @Override
  protected RequirementProvider createInline(final SourceClass enclosed, final SourceMethod method) {
    return new MethodImpl(enclosed, method, true);
  }

  @Override
  protected RequirementProvider createProvider(final SourceClass enclosed, final SourceMethod method) {
    return new MethodImpl(enclosed, method, false);
  }

  @Override
  protected RequirementProvider createField(final SourceClass enclosed, final SourceField field) {
    return new FieldImpl(enclosed, field);
  }

  @Override
  protected RequirementProvider createInstance(final SourceClass source) {
    return new InstanceImpl(source);
  }
}
