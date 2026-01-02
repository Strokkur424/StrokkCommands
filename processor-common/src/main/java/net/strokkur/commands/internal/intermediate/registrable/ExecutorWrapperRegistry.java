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
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.exceptions.ProviderAlreadyRegisteredException;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.MessagerWrapper;

import java.util.List;

public class ExecutorWrapperRegistry extends RegistrableRegistry<ExecutorWrapperProvider> {
  public ExecutorWrapperRegistry(final String platformType) {
    super(platformType);
  }

  /// - `Command<S> wrapper(Command<S>)`
  /// - `Command<S> wrapper(Command<S>, Method)`
  @Override
  public boolean tryRegisterProvider(
      final MessagerWrapper messager,
      final SourceClass annotationClass,
      final SourceElement sourceElement
  ) throws ProviderAlreadyRegisteredException {
    if (!(sourceElement instanceof SourceMethod sourceMethod)
        || sourceMethod.hasAnnotationInherited(Executes.class)
        || sourceMethod.hasAnnotationInherited(DefaultExecutes.class)) {
      return false;
    }

    if (!sourceMethod.getReturnType().getFullyQualifiedAndTypedName().equals(Classes.COMMAND + "<" + this.getPlatformType() + ">")) {
      messager.warnSource("Incorrect return type for executor wrapper.", sourceMethod);
      return false;
    }

    final List<SourceParameter> params = sourceMethod.getParameters();
    if (params.size() == 1 || params.size() == 2) {
      if (!params.getFirst().getType().getFullyQualifiedAndTypedName().equals(Classes.COMMAND + "<" + this.getPlatformType() + ">")) {
        messager.warnSource(
            "Incorrect parameter type. Expected %s<%s> but got: %s".formatted(
                Classes.COMMAND,
                this.getPlatformType(),
                params.getFirst().getType().getFullyQualifiedAndTypedName()
            ),
            params.getFirst()
        );
        return false;
      }

      if (params.size() == 2) {
        if (!params.get(1).getType().getFullyQualifiedAndTypedName().equals(Classes.METHOD)) {
          messager.warnSource(
              "Incorrect parameter type. Expected %s but got: %s".formatted(
                  Classes.METHOD,
                  params.get(1).getType().getFullyQualifiedAndTypedName()
              ),
              params.get(1)
          );
          return false;
        }

        this.registerProvider(annotationClass, new ExecutorWrapperProvider(sourceMethod, ExecutorWrapperProvider.WrapperType.COMMAND_METHOD));
      } else {
        this.registerProvider(annotationClass, new ExecutorWrapperProvider(sourceMethod, ExecutorWrapperProvider.WrapperType.COMMAND));
      }

      return true;
    }

    messager.warnSource("Incorrect number of parameters provided. Expected 1 or 2, but got: " + params.size(), sourceMethod);
    return false;
  }
}
