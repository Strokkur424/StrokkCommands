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

import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceType;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.ParameterType;
import net.strokkur.commands.internal.intermediate.executable.Parameterizable;
import net.strokkur.commands.internal.intermediate.executable.SourceParameterType;
import net.strokkur.commands.internal.intermediate.registrable.ExecutorWrapperProvider;
import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.internal.util.IOExceptionIgnoringConsumer;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;

interface TreePrinter<C extends CommandInformation> extends Printable, PrinterInformation<C>, ExecutorWrapperAccessible {

  String nextLiteral();

  void pushLiteral(String literal);

  void popLiteral();

  void popLiteralPosition();

  void prefixPrintExecutableInner(final CommandNode node, final Executable executable) throws IOException;

  String handleParameter(SourceVariable parameter) throws IOException;

  default void printNode(final CommandNode node) throws IOException {
    printNode(node, false);
  }

  private void printNode(final CommandNode root, boolean isNested) throws IOException {
    printForArguments(root, initializer -> {
      if (isNested) {
        println();
        printIndented(".then(");
        incrementIndent();
      }

      print(initializer);

      if (root.argument() instanceof RequiredCommandArgument req && req.hasAttribute(AttributeKey.SUGGESTION_PROVIDER)) {
        final SuggestionProvider provider = req.getAttributeNotNull(AttributeKey.SUGGESTION_PROVIDER);
        println();
        printIndented(".suggests(" + provider.getSuggestionString() + ")");
      }

      final RequirementProvider requirementProvider = root.getAttribute(AttributeKey.REQUIREMENT_PROVIDER);
      final String extraRequirement = getExtraRequirements(root);

      if (requirementProvider != null && extraRequirement == null) {
        println();
        printIndented(".requires(source -> %s)", requirementProvider.getRequirementString());
      } else if (requirementProvider == null && extraRequirement != null) {
        println();
        printIndented(".requires(source -> %s)", extraRequirement);
      } else if (requirementProvider != null) {
        println();
        printIndented(".requires(source -> %s && %s)", requirementProvider.getRequirementString(), extraRequirement);
      }

      final Executable executable = root.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
      if (executable != null) {
        printExecutableInner(root, executable);
      }

      for (final CommandNode node : root.children()) {
        printNode(node, true);
      }

      if (isNested) {
        decrementIndent();
        println();
        printIndented(")");
      }
    }, isNested);
  }

  private void printExecutableInner(final CommandNode node, final Executable executable) throws IOException {
    println();

    final ExecutorWrapperProvider wrapper = node.getAttributeOr(AttributeKey.EXECUTOR_WRAPPER, this.getExecutorWrapper());
    if (wrapper == null) {
      printExecutableInnerNoWrapper(node, executable);
    } else {
      printExecutableInnerWithWrapper(node, executable, wrapper);
    }
  }

  private void printExecutableInnerNoWrapper(final CommandNode node, final Executable executable) throws IOException {
    println(".executes(ctx -> {");
    incrementIndent();
    prefixPrintExecutableInner(node, executable);
    printExecutableBody(node, executable);
    println("return Command.SINGLE_SUCCESS;");
    decrementIndent();
    printIndented("})");
  }

  private void printExecutableInnerWithWrapper(final CommandNode node, final Executable executable, final ExecutorWrapperProvider wrapper) throws IOException {
    println(".executes(%s(ctx -> {", getWrapperMethodCall(wrapper));
    incrementIndent();
    prefixPrintExecutableInner(node, executable);
    printExecutableBody(node, executable);
    println("return Command.SINGLE_SUCCESS;");
    decrementIndent();

    if (wrapper.wrapperType().withMethod()) {
      final List<SourceParameter> params = executable.executesMethod().getParameters();
      final String parameterTypesString = params.isEmpty() ? "" : ", " + String.join(", ", executable.executesMethod().getParameters().stream()
          .map(SourceParameter::getType)
          .map(SourceType::getSourceName)
          .map((str) -> str + ".class")
          .toList());

      printIndented("}, getMethodViaReflection(%s.class, \"%s\"%s)))",
          executable.executesMethod().getEnclosed().getSourceName(),
          executable.executesMethod().getName(),
          parameterTypesString
      );
    } else {
      printIndented("}))");
    }
  }

  private String getWrapperMethodCall(final ExecutorWrapperProvider wrapper) {
    final SourceMethod method = wrapper.wrapperMethod();
    final String methodName = method.getName();

    if (wrapper.wrapperMethod().isStaticallyAccessible()) {
      // Static method: WrapperClass.wrapperMethod
      final SourceClass wrapperClass = wrapper.wrapperMethod().getEnclosed();
      return wrapperClass.getSourceName() + "." + methodName;
    } else {
      // Instance method: get the appropriate instance variable name
      return Utils.getInstanceName(this.getExecutorWrapperAccessStack()) + "." + methodName;
    }
  }

  private void printExecutableBody(final CommandNode node, final Executable executable) throws IOException {
    boolean instancePrint = true;
    CommandNode recordNode = node;

    do {
      final Parameterizable recordArguments = recordNode.getAttribute(AttributeKey.RECORD_ARGUMENTS);
      if (recordArguments != null) {
        printWithRecord(recordArguments, executable);
        instancePrint = false;
        break;
      }
    } while ((recordNode = recordNode.parent()) != null);

    if (instancePrint) {
      printWithInstance(executable);
    }
  }

  private void printWithInstance(final Executable executable) throws IOException {
    final List<ExecuteAccess<?>> pathToUse = getAccessStack();
    printExecutesMethodCall(executable, Utils.getInstanceName(pathToUse));
  }

  private void printExecutesMethodCall(final Executable executable, final String typeName) throws IOException {
    printIndented("{}.{}", typeName, executable.executesMethod().getName());
    printExecutesArguments(executable);
    print(";");
    println();
  }

  private void printWithRecord(final Parameterizable record, final Executable executable) throws IOException {
    final String typeName = executable.executesMethod().getEnclosed().getSourceName();

    if (record.parameterArguments().isEmpty()) {
      println("final {} executorClass = new {}();", typeName, typeName);
    } else {
      println("final {} executorClass = new {}(", typeName, typeName);
      incrementIndent();
      printArguments(record.parameterArguments().stream()
          .map(CommandArgument.class::cast)
          .toList());
      decrementIndent();
      println(");");
    }

    printExecutesMethodCall(executable, "executorClass");

    for (final ParameterType arg : record.parameterArguments()) {
      if (arg instanceof MultiLiteralCommandArgument) {
        popLiteralPosition();
      }
    }
  }

  private void printExecutesArguments(final Executable executable) throws IOException {
    final List<ParameterType> parameterArguments = executable.parameterArguments();
    if (parameterArguments.isEmpty()) {
      print("()");
      return;
    }

    if (parameterArguments.size() == 1) {
      print("(");
      switch (parameterArguments.getFirst()) {
        case CommandArgument argument -> printArgument(argument);
        case SourceParameterType(SourceVariable parameter) -> print(handleParameter(parameter));
      }
      print(")");
      return;
    }

    print("(");
    incrementIndent();
    println();
    for (int i = 0, parameterArgumentsSize = parameterArguments.size(); i < parameterArgumentsSize; i++) {
      final ParameterType parameterArgument = parameterArguments.get(i);
      printIndent();

      switch (parameterArgument) {
        case CommandArgument argument -> printArgument(argument);
        case SourceParameterType(SourceVariable parameter) -> print(handleParameter(parameter));
      }

      if (i + 1 < parameterArgumentsSize) {
        print(",");
        println();
      }
    }

    for (final ParameterType argument : parameterArguments) {
      if (argument instanceof MultiLiteralCommandArgument) {
        popLiteralPosition();
      }
    }

    decrementIndent();
    println();
    printIndented(")");
  }

  private void printArguments(final List<CommandArgument> arguments) throws IOException {
    for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
      printIndent();
      printArgument(arguments.get(i));

      if (i + 1 < argumentsSize) {
        print(",");
      }

      println();
    }
  }

  private void printArgument(final CommandArgument argument) throws IOException {
    print(switch (argument) {
      case RequiredCommandArgument req -> req.argumentType().retriever();
      case MultiLiteralCommandArgument ignored -> '"' + nextLiteral() + '"';
      case LiteralCommandArgument lit -> '"' + lit.literal() + '"';
      default -> throw new IllegalArgumentException("Unknown argument class: " + argument.getClass());
    });
  }

  @Nullable
  String getExtraRequirements(Attributable node);

  String getLiteralMethodString();

  String getArgumentMethodString();

  default String getCommandNameLiteralOverride(final LiteralCommandArgument lit) {
    return '"' + lit.literal() + '"';
  }

  private void printForArguments(final CommandNode node, final IOExceptionIgnoringConsumer<String> initializer, final boolean isNested) throws IOException {
    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      node.getAttributeNotNull(AttributeKey.ACCESS_STACK).forEach(getAccessStack()::push);
    }

    final ExecutorWrapperProvider current = this.getExecutorWrapper();

    // TODO: this bleeds over if a method has a wrapper declared and a method is present which starts with the same args and merges.
    // However, this is a cheap fix for a problem barely anyone will run into anyways and which can be solved with just one
    // UnsetExecutorWrapper annotation. Also it is 2:30 AM, cut me some slack.
    final ExecutorWrapperProvider executorWrapper = node.getAttribute(AttributeKey.EXECUTOR_WRAPPER);
    if (node.getAttributeNotNull(AttributeKey.EXECUTOR_WRAPPER_UNSET)) {
      this.updateExecutorWrapper(null);
    } else if (executorWrapper != null) {
      this.updateExecutorWrapper(executorWrapper);
    }

    switch (node.argument()) {
      case LiteralCommandArgument lit -> initializer.accept("%s(%s)".formatted(getLiteralMethodString(), isNested ? '"' + lit.literal() + '"' : getCommandNameLiteralOverride(lit)));
      case RequiredCommandArgument req -> initializer.accept("%s(\"%s\", %s)".formatted(getArgumentMethodString(), req.argumentName(), req.argumentType().initializer()));
      case MultiLiteralCommandArgument multi -> {
        for (final String literal : multi.literals()) {
          pushLiteral(literal);
          initializer.accept("%s(\"%s\")".formatted(getLiteralMethodString(), literal));
          popLiteral();
        }
      }
      default -> throw new IllegalArgumentException("Unknown argument class: " + node.argument().getClass());
    }

    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      node.getAttributeNotNull(AttributeKey.ACCESS_STACK).forEach(access -> getAccessStack().pop());
    }
    this.updateExecutorWrapper(current);
  }
}
