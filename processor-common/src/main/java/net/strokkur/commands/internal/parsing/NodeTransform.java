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
package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.Subcommand;
import net.strokkur.commands.internal.NodeUtils;
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceElement;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.exceptions.MismatchedArgumentTypeException;
import net.strokkur.commands.internal.exceptions.UnknownSenderException;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Function;

interface NodeTransform<S extends SourceElement> extends ForwardingMessagerWrapper {

  void transform(CommandNode parent, S element) throws MismatchedArgumentTypeException, UnknownSenderException;

  boolean requirement(S element);

  NodeUtils nodeUtils();

  @Override
  default MessagerWrapper delegateMessager() {
    return this.nodeUtils();
  }

  default void transformIfRequirement(CommandNode parent, S element) throws MismatchedArgumentTypeException, UnknownSenderException {
    if (requirement(element)) {
      transform(parent, element);
    }
  }

  default CommandNode createSubcommandNode(CommandNode parent, AnnotationsHolder element) throws MismatchedArgumentTypeException {
    final CommandNode node = createLiteralSequence(parent, element, Subcommand.class, Subcommand::value);
    return populateNode(parent, node, element);
  }

  @Contract("_,!null,_->!null;!null,_,_->!null")
  default CommandNode populateNode(final @Nullable CommandNode parent, final @Nullable CommandNode thisPath, final AnnotationsHolder element) {
    final CommandNode out = thisPath == null ? parent : thisPath;
    nodeUtils().platformUtils().populateNode(Objects.requireNonNull(out), element);
    return out;
  }

  @Nullable
  default <A extends Annotation> CommandNode createLiteralSequence(
      final CommandNode parent,
      final AnnotationsHolder element,
      final Class<A> annotation,
      final Function<A, @Nullable String> valueExtract) throws MismatchedArgumentTypeException {
    // Use inherited annotation lookup to support meta-annotations
    final A a = element.getAnnotationOptionalIncludingInherited(annotation).orElse(null);
    if (a == null) {
      return null;
    }

    final String path = valueExtract.apply(a);
    if (path == null || path.isBlank()) {
      return null;
    }

    CommandNode out = parent;
    for (final String literal : path.split(" ")) {
      out = out.addChild(LiteralCommandArgument.literal(literal, element));
    }
    return out;
  }
}
