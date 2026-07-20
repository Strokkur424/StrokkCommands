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

import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.executable.CommandParameter;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.source.annotation.AnnotationsHolder;
import net.strokkur.jap.source.classmodel.SourceParameterLike;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public interface PlatformUtils {

  static PlatformUtils get() {
    class Holder {
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
      static final Optional<PlatformUtils> INSTANCE = ServiceLoader.load(
        PlatformUtils.class, PlatformUtils.class.getClassLoader()
      ).findFirst();
    }

    return Holder.INSTANCE.orElseThrow(() -> new RuntimeException("No PlatformUtils provider registered."));
  }

  default void populateExecutesNode(Executable executable, CommandNode node, List<CommandParameter> parameters) throws UnknownSenderException {
    // noop
  }

  default boolean mayParameterBeArgument(SourceParameterLike param) {
    return true;
  }

  CodeClassType platformType();

  default void populateNode(AnnotationsHolder element, CommandNode node) {
    // noop
  }

  InvocationChainBuilder literalBuilder(ConvertToExpression name);

  InvocationChainBuilder argumentBuilder(ConvertToExpression name, ConvertToExpression argument);
}
