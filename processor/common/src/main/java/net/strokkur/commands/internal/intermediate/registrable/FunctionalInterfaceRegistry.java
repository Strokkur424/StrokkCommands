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
import net.strokkur.commands.internal.exceptions.ProviderAlreadyRegisteredException;
import net.strokkur.jap.code.util.Modifiers;
import net.strokkur.jap.source.classmodel.SourceAnnotationInterface;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceConstructor;
import net.strokkur.jap.source.classmodel.SourceElement;
import net.strokkur.jap.source.classmodel.SourceField;
import net.strokkur.jap.source.classmodel.SourceMethod;

public abstract class FunctionalInterfaceRegistry<T> extends RegistrableRegistry<T> {
  protected abstract boolean inlineMethodPredicate(SourceMethod source);

  protected abstract boolean providerMethodPredicate(SourceMethod source);

  protected abstract boolean instancePredicate(SourceClass source);

  protected abstract boolean fieldPredicate(SourceField source);

  protected abstract T createInline(SourceMethod method);

  protected abstract T createProvider(SourceMethod method);

  protected abstract T createField(SourceField field);

  protected abstract T createInstance(SourceClass source);

  public final boolean tryRegisterProvider(SourceAnnotationInterface annotationClass, SourceElement sourceElement)
    throws ProviderAlreadyRegisteredException {
    return switch (sourceElement) {
      case SourceMethod method -> {
        if (method instanceof SourceConstructor || method.hasAnnotationInherited(Executes.class) || method.hasAnnotationInherited(DefaultExecutes.class)) {
          yield false;
        }

        if (inlineMethodPredicate(method)) {
          if (!method.modifiers().contains(Modifiers.STATIC)) {
            infoSource("This method matches the @" + annotationClass.classType().simpleName() + " provider method, but is not static. Is this a mistake?", method);
            yield false;
          }
          registerProvider(annotationClass, createInline(method));
          yield true;
        }

        if (providerMethodPredicate(method)) {
          if (!method.modifiers().contains(Modifiers.STATIC)) {
            infoSource("This method matches the @" + annotationClass.classType().simpleName() + " provider method, but is not static. Is this a mistake?", method);
            yield false;
          }
          registerProvider(annotationClass, createProvider(method));
          yield true;
        }

        yield false;
      }
      case SourceField field -> {
        if (fieldPredicate(field)) {
          if (!field.modifiers().contains(Modifiers.STATIC)) {
            infoSource("This field matches the @" + annotationClass.classType().simpleName() + " provider field, but is not static. Is this a mistake?", field);
            yield false;
          }
          registerProvider(annotationClass, createField(field));
          yield true;
        }
        yield false;
      }
      case SourceClass type -> {
        if (instancePredicate(type)) {
          if (type.constructors().stream().anyMatch(ctor -> !ctor.parameters().isEmpty())) {
            infoSource("This class matches the @" + annotationClass.classType().simpleName() + " provider class, but is not statically accessible. Is this a mistake?", type);
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
