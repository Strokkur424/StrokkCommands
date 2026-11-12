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

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.jap.source.annotation.AnnotationsHolder;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

class AdvancedNodeExtender<A extends Annotation> {
  private final Class<A> annotationClass;
  private final Function<A, String> annotationToPathString;
  private @Nullable Class<? extends Annotation> pluralAnnotationsClass = null;
  private boolean alwaysRunHooks = true;

  private Consumer<CommandNode> firstNodeConsumer = AdvancedNodeExtender::nullConsumer;
  private Consumer<CommandNode> postProcessNode = AdvancedNodeExtender::nullConsumer;
  private List<List<CommandArgument>> arguments = List.of();

  AdvancedNodeExtender(Class<A> annotationClass, Function<A, String> toPath) {
    this.annotationClass = annotationClass;
    this.annotationToPathString = toPath;
  }

  private void acceptMaybePath(CommandNode node, boolean firstNodeFirst) {
    if (this.arguments.isEmpty()) {
      if (firstNodeFirst) {
        firstNodeConsumer.accept(node);
      }
      postProcessNode.accept(node);
      return;
    }

    for (List<CommandArgument> args : this.arguments) {
      if (args.isEmpty()) {
        if (firstNodeFirst) {
          firstNodeConsumer.accept(node);
        }
        postProcessNode.accept(node);
        continue;
      }
      final CommandNode first = node.addArgument(args.getFirst());
      if (firstNodeFirst) {
        firstNodeConsumer.accept(first);
      }
      postProcessNode.accept(args.size() == 1
        ? first
        : first.addArguments(args.subList(1, args.size()))
      );
    }
  }

  public void accept(AnnotationsHolder source, CommandNode root) {
    final List<A> pathAnnotations = source.getAnnotations(pluralAnnotationsClass, annotationClass);
    if (pathAnnotations.isEmpty()) {
      if (alwaysRunHooks) {
        this.acceptMaybePath(root, true);
      }
      return;
    }

    pathAnnotations.stream()
      .map(annotationToPathString)
      .distinct()
      .map(path -> path.isBlank() ?
        List.<LiteralCommandArgument>of() :
        Arrays.stream(path.strip().split(" "))
          .map(lit -> LiteralCommandArgument.literal(lit, false, false))
          .toList())
      .forEach(args -> {
        if (args.isEmpty()) {
          this.acceptMaybePath(root, true);
          return;
        }

        final CommandNode firstPathNode = root.addArgument(args.getFirst());
        firstNodeConsumer.accept(firstPathNode);
        final CommandNode endPathNode = args.size() == 1 ?
          firstPathNode :
          firstPathNode.addArguments(args.subList(1, args.size()));
        this.acceptMaybePath(endPathNode, false);
      });
  }

  public <N extends Annotation> AdvancedNodeExtender<N> withAnnotationClass(Class<N> annotationClass, Function<N, String> annotationToPathString) {
    final AdvancedNodeExtender<N> with = new AdvancedNodeExtender<>(annotationClass, annotationToPathString);
    with.firstNodeConsumer = this.firstNodeConsumer;
    with.postProcessNode = this.postProcessNode;
    with.arguments = this.arguments;
    with.alwaysRunHooks = this.alwaysRunHooks;
    return with;
  }

  public AdvancedNodeExtender<A> withPluralAnnotationsClass(@Nullable Class<? extends Annotation> pluralAnnotationsClass) {
    this.pluralAnnotationsClass = pluralAnnotationsClass;
    return this;
  }

  public AdvancedNodeExtender<A> withPostProcess(Consumer<CommandNode> postProcessNode) {
    this.postProcessNode = postProcessNode;
    return this;
  }

  public AdvancedNodeExtender<A> withFirstNodeConsumer(Consumer<CommandNode> firstNodeConsumer) {
    this.firstNodeConsumer = firstNodeConsumer;
    return this;
  }

  public AdvancedNodeExtender<A> withArguments(List<List<CommandArgument>> arguments) {
    this.arguments = arguments;
    return this;
  }

  public AdvancedNodeExtender<A> withAlwaysRunHooks(boolean alwaysRunHooks) {
    this.alwaysRunHooks = alwaysRunHooks;
    return this;
  }

  private static void nullConsumer(CommandNode ignored) {
    // noop
  }
}
