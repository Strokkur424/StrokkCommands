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
package net.strokkur.commands.internal;

import net.strokkur.commands.Literal;
import net.strokkur.commands.UnsetExecutorWrapper;
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgumentImpl;
import net.strokkur.commands.internal.exceptions.ConversionException;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.ParameterType;
import net.strokkur.commands.internal.intermediate.executable.SourceParameterType;
import net.strokkur.commands.internal.intermediate.registrable.ExecutorWrapperRegistry;
import net.strokkur.commands.internal.intermediate.registrable.RegistrableRegistry;
import net.strokkur.commands.internal.intermediate.registrable.RequirementRegistry;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionsRegistry;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import java.util.Optional;
import java.util.Set;

public record NodeUtils(
    PlatformUtils platformUtils,
    MessagerWrapper messager,
    BrigadierArgumentConverter converter,
    SuggestionsRegistry suggestionsRegistry,
    RequirementRegistry requirementRegistry,
    ExecutorWrapperRegistry executorWrapperRegistry
) implements ForwardingMessagerWrapper {

  public void applyExecutorTransform(final Attributable node, final AnnotationsHolder element) {
    if (element.hasAnnotationInherited(UnsetExecutorWrapper.class)) {
      node.setAttribute(AttributeKey.EXECUTOR_WRAPPER_UNSET, true);
      return;
    }

    this.applyRegistrableProvider(
        node,
        element,
        this.executorWrapperRegistry(),
        AttributeKey.EXECUTOR_WRAPPER,
        "executor wrapper"
    );
  }

  public ParameterType parseParameter(final SourceVariable parameter) {
    debug("| Parsing parameter: " + parameter.getName());

    if (!platformUtils().mayParameterBeArgument(parameter)) {
      return new SourceParameterType(parameter);
    }

    final Literal literal = parameter.getAnnotation(Literal.class);
    if (literal != null) {
      final String[] declared = literal.value();
      if (declared.length == 0) {
        return LiteralCommandArgument.literal(parameter.getName(), parameter);
      } else if (declared.length == 1) {
        return LiteralCommandArgument.literal(declared[0], parameter);
      } else {
        return MultiLiteralCommandArgument.multiLiteral(Set.of(declared), parameter);
      }
    }

    final BrigadierArgumentType argumentType;
    try {
      argumentType = converter.getAsArgumentType(parameter);
    } catch (ConversionException e) {
      return new SourceParameterType(parameter);
    }

    debug("  | Successfully found Brigadier type: {}", argumentType);
    final RequiredCommandArgument commandArgument = new RequiredCommandArgumentImpl(argumentType, parameter.getName(), parameter);
    applyRegistrableProvider(commandArgument, parameter, this.suggestionsRegistry, AttributeKey.SUGGESTION_PROVIDER, "suggestion");
    return commandArgument;
  }

  public <T> void applyRegistrableProvider(
      final Attributable attributable,
      final AnnotationsHolder element,
      final RegistrableRegistry<T> registry,
      final AttributeKey<T> key,
      final String name
  ) {
    boolean found = false;
    for (final SourceClass annotationType : element.getAllAnnotations()) {
      final Optional<T> provider = registry.getProvider(annotationType);
      if (provider.isPresent()) {
        if (found) {
          this.infoSource("Multiple %s providers has been declared", element, name);
        } else {
          attributable.setAttribute(key, provider.get());
          found = true;
        }
      }
    }
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return this.messager;
  }
}
