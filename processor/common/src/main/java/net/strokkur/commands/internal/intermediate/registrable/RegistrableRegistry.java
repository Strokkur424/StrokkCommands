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
import net.strokkur.commands.internal.abstraction.SourceElement;
import net.strokkur.commands.internal.exceptions.ProviderAlreadyRegisteredException;
import net.strokkur.commands.internal.util.MessagerWrapper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

public abstract class RegistrableRegistry<T> {
  private final Map<String, T> providerMap = new TreeMap<>();
  private final String platformType;

  public RegistrableRegistry(final String platformType) {
    this.platformType = platformType;
  }

  protected final String getPlatformType() {
    return this.platformType;
  }

  public final Set<String> getAllRegistrations() {
    return providerMap.keySet();
  }

  public final Optional<T> getProvider(final SourceClass annotationClass) {
    return Optional.ofNullable(this.providerMap.get(annotationClass.getFullyQualifiedName()));
  }

  public final void registerProvider(final SourceClass annotationClass, final T provider)
      throws ProviderAlreadyRegisteredException {
    if (this.providerMap.containsKey(annotationClass.getFullyQualifiedName())) {
      throw new ProviderAlreadyRegisteredException(annotationClass);
    }
    this.providerMap.put(annotationClass.getFullyQualifiedName(), provider);
  }

  public abstract boolean tryRegisterProvider(
      final MessagerWrapper messager,
      final SourceClass annotationClass,
      final SourceElement sourceElement
  ) throws ProviderAlreadyRegisteredException;
}
