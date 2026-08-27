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
package net.strokkur.commands.internal.prototype;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import net.strokkur.commands.internal.prototype.requirements.ComparableRequirement;
import net.strokkur.jap.code.classmodel.CodeBlock;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;
import net.strokkur.jap.code.util.StyleConfig;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class PrototypeNode {
  protected final List<PrototypeNode> children = new ArrayList<>();
  protected @Nullable PrototypeNode parent = null;

  public @Nullable SuggestionProvider suggests = null;
  private final Map<String, ComparableRequirement> requires = new HashMap<>();

  public @Nullable CodeBlock executes = null;
  public @Nullable CodeBlock defaultExecutes = null;

  protected abstract InvocationChainBuilder nodeElement();

  protected abstract String commandStringElement();

  public void addChild(PrototypeNode node) {
    children.add(node);
    node.parent = this;
  }

  public boolean addRequirement(ComparableRequirement req) {
    return requires.put(req.key(), req) == null;
  }

  public Map<String, ComparableRequirement> requires() {
    return requires;
  }

  @Unmodifiable
  public List<PrototypeNode> children() {
    return this.children;
  }

  private @Nullable CodeBlock getDefaultExecutes() {
    if (this.defaultExecutes != null) {
      return this.defaultExecutes;
    }
    if (this.parent != null) {
      return this.parent.getDefaultExecutes();
    }
    return null;
  }

  /// Run various pre-process tasks. This is currently only used to move around some requirements
  /// to upper level, in case multiple ones match together.
  public void preProcess() {
    this.children.forEach(PrototypeNode::preProcess);
    PlatformUtils.get().preProcess(this);
  }

  public InvocationChainBuilder toExpression() {
    final InvocationChainBuilder builder = nodeElement();
    if (!requires.isEmpty()) {
      builder.chainMethod("requires", StyleConfig.NEWLINE, Expressions.lambdaInline("source", requires.values().stream()
        .map(ConvertToExpression::toExpression)
        .reduce(ConvertToExpression::and)
        .orElseThrow()
      ));
    }
    if (suggests != null) {
      builder.chainMethod("suggests", StyleConfig.NEWLINE, suggests.toSuggestionLambda());
    }
    if (executes != null) {
      builder.chainMethod("executes", StyleConfig.NEWLINE, Expressions.lambda("ctx", executes));
    } else if (getDefaultExecutes() instanceof CodeBlock found) {
      builder.chainMethod("executes", StyleConfig.NEWLINE, Expressions.lambda("ctx", found));
    }

    for (PrototypeNode child : children) {
      builder.chainMethod("then", StyleConfig.NEWLINE_BOTH, child.toExpression());
    }

    return builder;
  }

  public String toCommandString() {
    if (parent == null) {
      return commandStringElement();
    }
    return parent.toCommandString() + " " + commandStringElement();
  }

  public <T extends PrototypeNode> Optional<T> findChild(Class<T> type, Predicate<T> node) {
    return children.stream()
      .filter(type::isInstance)
      .map(type::cast)
      .filter(node)
      .findFirst();
  }

  public void forEachChild(Consumer<PrototypeNode> run) {
    children.forEach(i -> i.forEachChildAndMyself(run));
  }

  private void forEachChildAndMyself(Consumer<PrototypeNode> run) {
    run.accept(this);
    children.forEach(i -> i.forEachChildAndMyself(run));
  }
}
