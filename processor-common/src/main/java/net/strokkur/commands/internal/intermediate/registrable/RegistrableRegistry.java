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

import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceElement;
import net.strokkur.commands.internal.abstraction.SourceField;
import net.strokkur.commands.internal.abstraction.SourceMethod;
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

  public Set<String> getAllRegistrations() {
    return providerMap.keySet();
  }

  public final Optional<T> getProvider(final SourceClass annotationClass) {
    return Optional.ofNullable(this.providerMap.get(annotationClass.getFullyQualifiedName()));
  }

  protected String getPlatformType() {
    return this.platformType;
  }

  protected abstract boolean inlineMethodPredicate(SourceMethod source);

  protected abstract boolean providerMethodPredicate(SourceMethod source);

  protected abstract boolean instancePredicate(SourceClass source);

  protected abstract boolean fieldPredicate(SourceField source);

  protected abstract T createInline(SourceClass enclosed, SourceMethod method);

  protected abstract T createProvider(SourceClass enclosed, SourceMethod method);

  protected abstract T createField(SourceClass enclosed, SourceField field);

  protected abstract T createInstance(SourceClass source);

  public final void registerProvider(final SourceClass annotationClass, final T provider)
      throws ProviderAlreadyRegisteredException {
    if (this.providerMap.containsKey(annotationClass.getFullyQualifiedName())) {
      throw new ProviderAlreadyRegisteredException(annotationClass);
    }
    this.providerMap.put(annotationClass.getFullyQualifiedName(), provider);
  }

  public final boolean tryRegisterProvider(final MessagerWrapper messager, final SourceClass annotationClass, final SourceElement sourceElement)
      throws ProviderAlreadyRegisteredException {
    return switch (sourceElement) {
      case SourceMethod method -> {
        if (method.isConstructor() || method.hasAnnotation(Executes.class) || method.hasAnnotation(DefaultExecutes.class)) {
          yield false;
        }

        if (inlineMethodPredicate(method)) {
          if (!method.isStaticallyAccessible()) {
            messager.infoSource("This method matches the @" + annotationClass.getName() + " provider method, but is not static. Is this a mistake?", method);
            yield false;
          }
          registerProvider(annotationClass, createInline(method.getEnclosed(), method));
          yield true;
        }

        if (providerMethodPredicate(method)) {
          if (!method.isStaticallyAccessible()) {
            messager.infoSource("This method matches the @" + annotationClass.getName() + " provider method, but is not static. Is this a mistake?", method);
            yield false;
          }
          registerProvider(annotationClass, createProvider(method.getEnclosed(), method));
          yield true;
        }

        yield false;
      }
      case SourceField field -> {
        if (fieldPredicate(field)) {
          if (!field.isStaticallyAccessible()) {
            messager.infoSource("This field matches the @" + annotationClass.getName() + " provider field, but is not static. Is this a mistake?", field);
            yield false;
          }
          registerProvider(annotationClass, createField(field.getEnclosed(), field));
          yield true;
        }
        yield false;
      }
      case SourceClass type -> {
        if (instancePredicate(type)) {
          if (type.hasNonStaticConstructor()) {
            messager.infoSource("This class matches the @" + annotationClass.getName() + " provider class, but is not statically accessible. Is this a mistake?", type);
            yield false;
          }
          registerProvider(annotationClass, createInstance(type));
          yield true;
        }
        yield false;
      }
      default -> false;
    };
  }
}
