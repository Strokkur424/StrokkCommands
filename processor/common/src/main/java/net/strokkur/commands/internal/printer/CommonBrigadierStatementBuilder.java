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

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.UnparsedCommandParameter;
import net.strokkur.commands.internal.intermediate.record.RecordArguments;
import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import net.strokkur.commands.internal.intermediate.tree.ArgumentNode;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToFieldMethodSource;
import net.strokkur.jap.code.convert.ConvertToStatement;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.ConstructorInvocationBuilder;
import net.strokkur.jap.code.expression.builder.InvocationChainBuilder;
import net.strokkur.jap.code.expression.builder.MethodInvocationBuilder;
import net.strokkur.jap.code.statement.Statements;
import net.strokkur.jap.code.util.StyleConfig;
import net.strokkur.jap.source.classmodel.SourceParameterLike;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

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
  private final Stack<RecordArguments> recordStack = new Stack<>();
  private int literalPointer = 0;

  protected abstract InvocationChainBuilder literalBuilder(ConvertToExpression name);

  protected abstract InvocationChainBuilder argumentBuilder(ConvertToExpression name, ConvertToExpression argument);

  protected abstract List<? extends ConvertToStatement> validationStatements(Executable executable);

  protected abstract ConvertToExpression convertUnparsedParameter(SourceParameterLike parameter);

  public final ConvertToExpression build(CommandNode node, ConvertToExpression rootNameExpression) {
    final InvocationChainBuilder builder = literalBuilder(rootNameExpression);
    createTree(builder, node);
    builder.chainMethod("build", StyleConfig.NEWLINE);
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
    if (!recordStack.isEmpty()) {
      System.err.println("The record stack was not empty; something is leaking resources.");
      recordStack.clear();
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

  protected void createTree(InvocationChainBuilder builder, CommandNode node) {
    scopeAccessStack(node, () -> scopeRecordStack(node.getAttribute(AttributeKey.RECORD_ARGUMENTS), () -> {
      if (node instanceof ArgumentNode argumentNode) {
        populateNode(builder, argumentNode);
      }
      for (CommandNode child : node.children()) {
        appendNode(builder, child);
      }
    }));
  }

  protected void populateNode(InvocationChainBuilder builder, ArgumentNode node) {
    // Requirements
    if (node.hasAttribute(AttributeKey.REQUIREMENT_PROVIDER)) {
      final RequirementProvider provider = node.getAttributeNotNull(AttributeKey.REQUIREMENT_PROVIDER);
      builder.chainMethod("requires", StyleConfig.NEWLINE, provider.toRequirementLambda());
    }

    // Suggestions
    if (node.argument() instanceof RequiredCommandArgument req && req.hasAttribute(AttributeKey.SUGGESTION_PROVIDER)) {
      final SuggestionProvider provider = req.getAttributeNotNull(AttributeKey.SUGGESTION_PROVIDER);
      builder.chainMethod("suggests", StyleConfig.NEWLINE, provider.toSuggestionLambda());
    }

    final Executable executable = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      scopeLiteralAccess(() -> {
        builder.chainMethod("executes", StyleConfig.NEWLINE, getExecutesExpression(node, executable));
      });
    }
  }

  private ConvertToExpression getExecutesExpression(CommandNode node, Executable executable) {
    final List<ConvertToStatement> statements = new ArrayList<>(validationStatements(executable));

    if (!recordStack.isEmpty()) {
      final RecordArguments args = recordStack.peek();
      final ConstructorInvocationBuilder builder = args.record().ctor();
      if (args.parameters().size() > 2) {
        builder.setStyle(StyleConfig.MULTILINE);
      }

      args.parameters().stream()
        .map(param -> switch (param) {
          case CommandArgument arg -> getArgumentValueExpr(arg);
          case UnparsedCommandParameter(SourceParameterLike parameter) -> convertUnparsedParameter(parameter);
        }).forEach(builder::addParameters);

      statements.add(createCallStatement(builder, executable));
    } else {
      final PrintedAccessPath path = new PrintedAccessPath(accessStack);
      statements.add(createCallStatement(path.getVariableAccess(), executable));
      requiredPaths.add(path);
    }

    statements.add(Statements.returnStmt(Classes.COMMAND.chainField("SINGLE_SUCCESS")));
    return Expressions.lambda("ctx", statements.toArray(ConvertToStatement[]::new));
  }

  private ConvertToStatement createCallStatement(ConvertToFieldMethodSource source, Executable executable) {
    final MethodInvocationBuilder builder = source.chainMethod(executable.executesMethod().name());

    executable.parameters()
      .forEach(arg -> builder.addParameters(switch (arg) {
        case CommandArgument argument -> getArgumentValueExpr(argument);
        case UnparsedCommandParameter(SourceParameterLike parameter) -> convertUnparsedParameter(parameter);
      }));

    return builder;
  }

  private ConvertToExpression getArgumentValueExpr(CommandArgument argument) {
    return switch (argument) {
      case RequiredCommandArgument required -> required.argumentType().retriever();
      case LiteralCommandArgument ignored -> Expressions.string(nextLiteral());
      case MultiLiteralCommandArgument ignored -> Expressions.string(nextLiteral());
      default -> throw new IllegalStateException("Unexpected argument type: " + argument.getClass().getName());
    };
  }

  protected void appendNode(InvocationChainBuilder builder, CommandNode node) {
    if (!(node instanceof ArgumentNode argNode)) {
      for (CommandNode child : node.children()) {
        appendNode(builder, child);
      }
      return;
    }

    switch (argNode.argument()) {
      case LiteralCommandArgument literal -> {
        scopeLiteral(literal.literal(), () -> {
          final InvocationChainBuilder nested = literalBuilder(Expressions.string(literal.literal()));
          createTree(nested, node);
          builder.chainMethod("then", StyleConfig.NEWLINE_BOTH, nested);
        });
      }
      case RequiredCommandArgument required -> {
        final InvocationChainBuilder nested = argumentBuilder(
          Expressions.string(required.argumentName()),
          required.argumentType().initializer()
        );
        createTree(nested, node);
        builder.chainMethod("then", StyleConfig.NEWLINE_BOTH, nested);
      }
      case MultiLiteralCommandArgument multiLiteral -> {
        for (String literal : multiLiteral.literals()) {
          scopeLiteral(literal, () -> {
            final InvocationChainBuilder nested = literalBuilder(Expressions.string(literal));
            createTree(nested, node);
            builder.chainMethod("then", StyleConfig.NEWLINE_BOTH, nested);
          });
        }
      }
      default ->
        throw new IllegalArgumentException("Unknown argument class: " + argNode.argument().getClass().getName());
    }
  }

  protected final String nextLiteral() {
    return literalStack.get(literalPointer++);
  }

  protected final void scopeAccessStack(CommandNode node, Runnable run) {
    final Optional<ExecuteAccess<?>> access = node.getAttributeOptional(AttributeKey.ACCESS);
    access.ifPresent(accessStack::push);
    run.run();
    access.ifPresent(ignored -> accessStack.pop());
  }

  protected final void scopeRecordStack(@Nullable RecordArguments record, Runnable run) {
    if (record != null) {
      recordStack.push(record);
      run.run();
      recordStack.pop();
    } else {
      run.run();
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
