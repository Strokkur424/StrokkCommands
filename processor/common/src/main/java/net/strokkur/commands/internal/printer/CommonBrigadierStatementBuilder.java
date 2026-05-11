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
package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.InvokesMethod;
import net.strokkur.commands.internal.codegen.adapter.CodeTypeAdapter;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.as.AsStatement;
import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.codegen.builder.MethodInvocationBuilder;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.Parameterizable;
import net.strokkur.commands.internal.intermediate.executable.SourceParameterType;
import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.Classes;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

public abstract class CommonBrigadierStatementBuilder {
  protected final Set<PrintedAccessPath> requiredPaths = new HashSet<>();
  private final Stack<ExecuteAccess<?>> accessStack = new Stack<>();
  private final Stack<String> literalStack = new Stack<>();
  private int literalPointer = 0;

  protected abstract MethodInvocationBuilder literalBuilder(AsExpression name);

  protected abstract MethodInvocationBuilder argumentBuilder(AsExpression name, AsExpression argument);

  protected abstract List<AsStatement> validationStatements(Executable executable);

  public final AsExpression build(CommandNode node, AsExpression rootNameExpression) {
    final MethodInvocationBuilder builder = literalBuilder(rootNameExpression);
    fill(builder, node);
    builder.chain("build", InvokesMethod.StyleConfig.NEWLINE);
    return builder;
  }

  /// Resets the state of this builder to the original state for reuse.
  @MustBeInvokedByOverriders
  protected void reset() {
    requiredPaths.clear();
    if (!accessStack.isEmpty()) {
      System.err.println("The access stack was not empty; something is leaking resources.");
      accessStack.clear();
    }
    if (!literalStack.isEmpty()) {
      System.err.println("The literal stack was not empty; something is leaking resources.");
      literalStack.clear();
    }
    if (literalPointer != 0) {
      System.err.printf("The literal pointer was not 0 (was: %s); something is leaking resources.%n", literalPointer);
      literalPointer = 0;
    }
  }

  protected void fill(MethodInvocationBuilder builder, CommandNode node) {
    scopeAccessStack(node, () -> {
      populateNode(builder, node);
      for (CommandNode child : node.children()) {
        appendNode(builder, child);
      }
    });
  }

  protected void populateNode(MethodInvocationBuilder builder, CommandNode node) {
    // Requirements
    if (node.hasAttribute(AttributeKey.REQUIREMENT_PROVIDER)) {
      final RequirementProvider provider = node.getAttributeNotNull(AttributeKey.REQUIREMENT_PROVIDER);
      builder.chain("requires", InvokesMethod.StyleConfig.NEWLINE, provider.getRequirementExpression());
    }

    // Suggestions
    if (node.argument() instanceof RequiredCommandArgument req && req.hasAttribute(AttributeKey.SUGGESTION_PROVIDER)) {
      final SuggestionProvider provider = req.getAttributeNotNull(AttributeKey.SUGGESTION_PROVIDER);
      builder.chain("suggests", InvokesMethod.StyleConfig.NEWLINE, provider.getSuggestionExpression());
    }

    final Executable executable = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      scopeLiteralAccess(() -> {
        builder.chain("executes", InvokesMethod.StyleConfig.NEWLINE, getExecutesExpression(node, executable));
      });
    }
  }

  private AsExpression getExecutesExpression(CommandNode node, Executable executable) {
    final List<AsStatement> statements = new ArrayList<>(validationStatements(executable));

    if (node.parent() instanceof CommandNode parent && parent.getAttribute(AttributeKey.RECORD_ARGUMENTS) instanceof Parameterizable recordArguments) {
      // Method defined inside a record class
      final MethodInvocationBuilder builder = Builders.ctorInvocation(CodeTypeAdapter.from(executable.executesMethod().getEnclosed()));
      if (recordArguments.parameterArguments().size() > 2) {
        builder.setMultilineParameters();
      }

      recordArguments.parameterArguments().stream()
          .map(CommandArgument.class::cast)
          .forEach(arg -> builder.addParameter(getArgumentValueExpr(arg)));

      statements.add(createCallStatement(builder.getAsExpression(), executable));
    } else {
      final PrintedAccessPath path = new PrintedAccessPath(accessStack);
      statements.add(createCallStatement(path.getVariableAccess(), executable));
      requiredPaths.add(path);
    }

    statements.add(CodeStatement.returnStatement(Builders.fieldAccess("SINGLE_SUCCESS").setStatic(Classes.COMMAND)));
    return CodeExpression.lambda(List.of("ctx"), statements.toArray(AsStatement[]::new));
  }

  private AsStatement createCallStatement(AsExpression source, Executable executable) {
    final MethodInvocationBuilder builder = Builders.methodInvocation(executable.executesMethod().getName())
        .setInstanceSource(source);

    executable.parameterArguments()
        .forEach(arg -> builder.addParameter(switch (arg) {
          case CommandArgument argument -> getArgumentValueExpr(argument);
          case SourceParameterType(SourceVariable parameter) -> getParameterValueExpr(parameter);
        }));

    return builder;
  }

  private AsExpression getArgumentValueExpr(CommandArgument argument) {
    return switch (argument) {
      case RequiredCommandArgument required -> required.argumentType().retriever();
      case LiteralCommandArgument ignored -> CodeExpression.string(nextLiteral());
      case MultiLiteralCommandArgument ignored -> CodeExpression.string(nextLiteral());
      default -> throw new IllegalStateException("Unexpected argument type: " + argument.getClass().getName());
    };
  }

  protected abstract AsExpression getParameterValueExpr(SourceVariable parameter);

  protected void appendNode(MethodInvocationBuilder builder, CommandNode node) {
    switch (node.argument()) {
      case LiteralCommandArgument literal -> {
        scopeLiteral(literal.literal(), () -> {
          final MethodInvocationBuilder nested = literalBuilder(CodeExpression.string(literal.literal()));
          fill(nested, node);
          builder.chain("then", InvokesMethod.StyleConfig.NEWLINE_BOTH, nested);
        });
      }
      case RequiredCommandArgument required -> {
        final MethodInvocationBuilder nested = argumentBuilder(
            CodeExpression.string(required.argumentName()),
            required.argumentType().initializer()
        );
        fill(nested, node);
        builder.chain("then", InvokesMethod.StyleConfig.NEWLINE_BOTH, nested);
      }
      case MultiLiteralCommandArgument multiLiteral -> {
        for (String literal : multiLiteral.literals()) {
          scopeLiteral(literal, () -> {
            final MethodInvocationBuilder nested = literalBuilder(CodeExpression.string(literal));
            fill(nested, node);
            builder.chain("then", InvokesMethod.StyleConfig.NEWLINE_BOTH, nested);
          });
        }
      }
      default -> throw new IllegalArgumentException("Unknown argument class: " + node.argument().getClass().getName());
    }
  }

  protected final String nextLiteral() {
    return literalStack.get(literalPointer++);
  }

  protected final void scopeAccessStack(CommandNode node, Runnable run) {
    final Optional<List<ExecuteAccess<?>>> access = node.getAttributeOptional(AttributeKey.ACCESS_STACK);
    final int numberOfPushes = access.map(List::size).orElse(0);
    access.ifPresent(list -> list.forEach(accessStack::push));

    run.run();

    for (int i = 0; i < numberOfPushes; i++) {
      accessStack.pop();
    }
  }

  protected final void scopeLiteral(String literal, Runnable run) {
    literalStack.push(literal);
    run.run();
    literalStack.pop();
  }

  protected final void scopeLiteralAccess(Runnable run) {
    final int pointerPosition = literalPointer;
    run.run();
    literalPointer = pointerPosition;
  }
}
