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
import net.strokkur.commands.internal.printer.PrintedAccessPath;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.jap.code.classmodel.CodeBlock;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToFieldMethodSource;
import net.strokkur.jap.code.convert.ConvertToStatement;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.ConstructorInvocationBuilder;
import net.strokkur.jap.code.expression.builder.MethodInvocationBuilder;
import net.strokkur.jap.code.statement.Statements;
import net.strokkur.jap.code.util.StyleConfig;
import net.strokkur.jap.source.classmodel.SourceParameterLike;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Stack;

/// This class is supposed to be re-created each time a new command class is printed.
/// For this reason, no reset method, or similar, is provided.
public abstract class PrototypeNodeBuilder {
  private final Stack<RecordArguments> recordStack = new Stack<>();

  protected final Set<PrintedAccessPath> requiredPaths = new HashSet<>();
  private final Deque<ExecuteAccess<?>> accessStack = new ArrayDeque<>();
  private final Deque<String> literalQueue = new ArrayDeque<>();

  protected final List<String> warnings = new ArrayList<>();
  protected final List<String> errors = new ArrayList<>();

  public static PrototypeNodeBuilder create() {
    return ServiceLoader.load(PrototypeNodeBuilder.class, PrototypeNode.class.getClassLoader())
      .findFirst()
      .orElseThrow(() -> new RuntimeException("No implementation of PrototypeNodeBuilder found."));
  }

  /// Gets initial validation statements to be put into an executes-block. This is used for stuff
  /// like sender filtering.
  protected abstract List<? extends ConvertToStatement> validationStatements(Executable executable);

  /// Converts a non-argument parameter into an expression.
  protected abstract ConvertToExpression convertUnparsedParameter(SourceParameterLike parameter);

  public PrototypeRoot createRoot(CommandNode rootNode) {
    final PrototypeRoot root = new PrototypeRoot(rootNode.name());

    scopeAccessStack(rootNode, () -> {
      handleAttributes(root, rootNode);
      for (CommandNode child : rootNode.children()) {
        appendTo(root, child);
      }
    });

    return root;
  }

  @MustBeInvokedByOverriders
  protected void handleAttributes(PrototypeNode prototype, CommandNode node) {
    if (node.hasAttribute(AttributeKey.SUGGESTION_PROVIDER)) {
      final SuggestionProvider provider = node.getAttributeNotNull(AttributeKey.SUGGESTION_PROVIDER);
      if (prototype.suggests == null) {
        prototype.suggests = provider;
      } else if (!prototype.suggests.equals(provider)) {
        warnings.add("Command with path '" + prototype.toCommandString() + "' has conflicting suggestion providers!");
      }
    }

    if (node.hasAttribute(AttributeKey.REQUIREMENT_PROVIDER)) {
      final RequirementProvider provider = node.getAttributeNotNull(AttributeKey.REQUIREMENT_PROVIDER);
      if (prototype.requires == null) {
        prototype.requires = provider;
      } else if (!prototype.requires.equals(provider)) {
        warnings.add("Command with path '" + prototype.toCommandString() + "' has duplicate requirement providers!");
      }
    }

    final Executable executable = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      if (prototype.executes == null) {
        fillLiteralQueue(prototype);
        prototype.executes = getExecutesBlock(executable);
        if (!literalQueue.isEmpty()) {
          warnings.add("Literal queue was not empty after code block for path '" + prototype.toCommandString() + "'. You should report this.");
          literalQueue.clear();
        }
      } else {
        warnings.add("Command with path '" + prototype.toCommandString() + "' has conflicting executes declarations!");
      }
    }
  }

  private void appendTo(PrototypeNode prototype, CommandNode node) {
    scopeAccessStack(node, () -> {
      if (node instanceof ArgumentNode arg) {
        switch (arg.argument()) {
          case LiteralCommandArgument(String lit, boolean isArgumentParam) -> appendLiteralTo(prototype, node, lit, isArgumentParam);
          case MultiLiteralCommandArgument(Set<String> literals) -> {
            literals.forEach(lit -> appendLiteralTo(prototype, node, lit, true));
          }
          case RequiredCommandArgument req -> {
            final Optional<PrototypeArgument> found = prototype.findChild(
              PrototypeArgument.class,
              p -> p.argumentType.equals(req.argumentType()) || p.name.equals(req.argumentName())
            );

            final PrototypeArgument next;
            if (found.isEmpty()) {
              next = new PrototypeArgument(req.argumentName(), req.argumentType());
              prototype.addChild(next);
            } else {
              next = found.get();

              // Do validation to ensure both argument type and name match, otherwise something went wrong.
              if (!next.argumentType.argumentTypeIdentifier().equals(req.argumentType().argumentTypeIdentifier())) {
                // Different argument type, but same name.
                errors.add("Argument named " + req.argumentName() + " defined twice with different types. (Path: " + next.toCommandString() + ")");
                return;
              } else if (!next.name.equals(req.argumentName())) {
                // Different argument names, but same type.
                errors.add("Argument named %s clashes with argument named %s, as they have the same type. (Path: %s)".formatted(
                  req.argumentName(), next.name, next.toCommandString()
                ));
                return;
              }
            }
            handleAttributes(next, node);
            node.children().forEach(child -> appendTo(next, child));
          }
          default -> throw new IllegalStateException("Illegal argument node type: " + arg.argument().getClass());
        }
      } else {
        handleAttributes(prototype, node);
        node.children().forEach(child -> appendTo(prototype, child));
      }
    });
  }

  private void appendLiteralTo(PrototypeNode prototype, CommandNode node, String literal, boolean argumentParam) {
    final Optional<PrototypeLiteral> found = prototype.findChild(PrototypeLiteral.class, p -> p.literal.equals(literal));
    final PrototypeNode next;
    if (found.isEmpty()) {
      next = new PrototypeLiteral(literal, argumentParam);
      prototype.addChild(next);
    } else {
      next = found.get();
    }

    handleAttributes(next, node);
    node.children().forEach(child -> appendTo(next, child));
  }

  //
  // Executes block creation.
  //

  private CodeBlock getExecutesBlock(Executable executable) {
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
      final PrintedAccessPath path = PrintedAccessPath.of(accessStack.reversed());
      statements.add(createCallStatement(path.getVariableAccess(), executable));
      addRequiredPath(path);
    }

    statements.add(Statements.returnStmt(Classes.COMMAND.chainField("SINGLE_SUCCESS")));
    return CodeBlock.of(statements);
  }

  private ConvertToStatement createCallStatement(ConvertToFieldMethodSource source, Executable executable) {
    final MethodInvocationBuilder builder = source.chainMethod(executable.executesMethod().name());

    executable.parameters()
      .forEach(arg -> builder.addParameters(switch (arg) {
        case CommandArgument argument -> getArgumentValueExpr(argument);
        case UnparsedCommandParameter(SourceParameterLike parameter) -> convertUnparsedParameter(parameter);
      }));

    if (executable.parameters().size() > 1) {
      builder.setStyle(StyleConfig.MULTILINE);
    }

    return builder;
  }

  private ConvertToExpression getArgumentValueExpr(CommandArgument argument) {
    return switch (argument) {
      case RequiredCommandArgument required -> required.argumentType().retriever();
      case LiteralCommandArgument ignored -> Expressions.string(literalQueue.pop());
      case MultiLiteralCommandArgument ignored -> Expressions.string(literalQueue.pop());
      default -> throw new IllegalStateException("Unexpected argument type: " + argument.getClass().getName());
    };
  }

  private void fillLiteralQueue(PrototypeNode node) {
    if (node instanceof PrototypeLiteral literal && literal.isArgumentParam) {
      literalQueue.push(literal.literal);
    }
    if (node.parent != null) {
      fillLiteralQueue(node.parent);
    }
  }

  private void scopeAccessStack(CommandNode node, Runnable run) {
    final Optional<ExecuteAccess<?>> access = node.getAttributeOptional(AttributeKey.ACCESS);
    access.ifPresent(accessStack::push);
    run.run();
    access.ifPresent(ignored -> accessStack.pop());
  }

  private void addRequiredPath(PrintedAccessPath path) {
    if (path.needsCreating()) {
      requiredPaths.add(path);
    } else if (path.requiresParent()) {
      requiredPaths.add(path.requiredParent());
    }
  }

  //
  // Getters
  //

  public Set<PrintedAccessPath> requiredPaths() {
    return requiredPaths;
  }

  public List<String> warnings() {
    return warnings;
  }

  public List<String> errors() {
    return errors;
  }
}
