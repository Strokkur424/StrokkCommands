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
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.attributes.ExecutorWrapperProvider;
import net.strokkur.commands.internal.intermediate.attributes.ExecutorWrapperProvider.WrapperType;
import net.strokkur.commands.internal.intermediate.attributes.Parameterizable;
import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.internal.util.IOExceptionIgnoringConsumer;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

interface TreePrinter<C extends CommandInformation> extends Printable, PrinterInformation<C> {

  String nextLiteral();

  void pushLiteral(String literal);

  void popLiteral();

  void popLiteralPosition();

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

  void prefixPrintExecutableInner(final CommandNode node, final Executable executable) throws IOException;

  private void printExecutableInner(final CommandNode node, final Executable executable) throws IOException {
    final ExecutorWrapperProvider wrapper = node.getAttribute(AttributeKey.EXECUTOR_WRAPPER);

    println();

    if (wrapper == null) {
      printExecutableInnerNoWrapper(node, executable);
    } else {
      switch (wrapper.wrapperType()) {
        case COMMAND_WRAPPER -> printExecutableInnerCommandWrapper(node, executable, wrapper);
        case INT_EXECUTOR -> printExecutableInnerIntExecutor(node, executable, wrapper);
        case VOID_EXECUTOR -> printExecutableInnerVoidExecutor(node, executable, wrapper);
      }
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

  private void printExecutableInnerCommandWrapper(final CommandNode node, final Executable executable, final ExecutorWrapperProvider wrapper) throws IOException {
    // Command<S> wrap(Command<S> executor, Method method)
    // Output: .executes(WrapperClass.wrapperMethod(ctx -> { ... }, getMethod_...()))
    final String wrapperCall = getWrapperMethodCall(wrapper);
    println(".executes(%s(ctx -> {", wrapperCall);
    incrementIndent();
    prefixPrintExecutableInner(node, executable);
    printExecutableBody(node, executable);
    println("return Command.SINGLE_SUCCESS;");
    decrementIndent();
    printIndented("}, %s()))", getMethodHelperName(executable.executesMethod()));
  }

  private void printExecutableInnerIntExecutor(final CommandNode node, final Executable executable, final ExecutorWrapperProvider wrapper) throws IOException {
    // int execute(CommandContext<S> ctx, Command<S> executor, Method method)
    // Output: .executes(ctx -> WrapperClass.wrapperMethod(ctx, ctx2 -> { ... }, getMethod_...()))
    final String wrapperCall = getWrapperMethodCall(wrapper);
    println(".executes(ctx -> %s(ctx, ctx2 -> {", wrapperCall);
    incrementIndent();
    prefixPrintExecutableInner(node, executable);
    printExecutableBody(node, executable);
    println("return Command.SINGLE_SUCCESS;");
    decrementIndent();
    printIndented("}, %s()))", getMethodHelperName(executable.executesMethod()));
  }

  private void printExecutableInnerVoidExecutor(final CommandNode node, final Executable executable, final ExecutorWrapperProvider wrapper) throws IOException {
    // void execute(CommandContext<S> ctx, Command<S> executor, Method method)
    // Output: .executes(ctx -> { WrapperClass.wrapperMethod(ctx, ctx2 -> { ... }, getMethod_...()); return Command.SINGLE_SUCCESS; })
    final String wrapperCall = getWrapperMethodCall(wrapper);
    println(".executes(ctx -> {");
    incrementIndent();
    println("%s(ctx, ctx2 -> {", wrapperCall);
    incrementIndent();
    prefixPrintExecutableInner(node, executable);
    printExecutableBody(node, executable);
    println("return Command.SINGLE_SUCCESS;");
    decrementIndent();
    println("}, %s());", getMethodHelperName(executable.executesMethod()));
    println("return Command.SINGLE_SUCCESS;");
    decrementIndent();
    printIndented("})");
  }

  private String getWrapperMethodCall(final ExecutorWrapperProvider wrapper) {
    final SourceMethod method = wrapper.wrapperMethod();
    final String methodName = method.getName();

    if (wrapper.isStatic()) {
      // Static method: WrapperClass.wrapperMethod
      final SourceClass wrapperClass = wrapper.wrapperClass();
      return wrapperClass.getFullyQualifiedName() + "." + methodName;
    } else {
      // Instance method: get the appropriate instance variable name
      return getWrapperInstanceName(wrapper) + "." + methodName;
    }
  }

  String getWrapperInstanceName(ExecutorWrapperProvider wrapper);

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

  void registerMethodHelper(SourceMethod method);

  default String getMethodHelperName(final SourceMethod method) {
    registerMethodHelper(method);
    final String className = method.getEnclosed().getName();
    final String methodName = method.getName();
    final List<SourceParameter> params = method.getParameters();

    final StringBuilder sb = new StringBuilder("getMethod_").append(className).append("_").append(methodName);
    for (final SourceParameter param : params) {
      sb.append("_").append(param.getType().getFullyQualifiedName().replace(".", "_"));
    }
    return sb.toString();
  }

  private void printWithInstance(final Executable executable) throws IOException {
    final List<ExecuteAccess<?>> pathToUse = getAccessStack();
    printExecutesMethodCall(executable, Utils.getInstanceName(pathToUse));
  }

  private void printExecutesMethodCall(final Executable executable, final String typeName) throws IOException {
    println("{}.{}(", typeName, executable.executesMethod().getName());
    incrementIndent();

    // Arguments
    printExecutesArguments(executable);

    decrementIndent();
    println(");");
  }

  private void printWithRecord(final Parameterizable record, final Executable executable) throws IOException {
    final String typeName = executable.executesMethod().getEnclosed().getSourceName();

    if (record.parameterArguments().isEmpty()) {
      println("final {} executorClass = new {}();", typeName, typeName);
    } else {
      println("final {} executorClass = new {}(", typeName, typeName);
      incrementIndent();
      printArguments(record.parameterArguments());
      decrementIndent();
      println(");");
    }

    printExecutesMethodCall(executable, "executorClass");

    for (final CommandArgument arguments : record.parameterArguments()) {
      if (arguments instanceof MultiLiteralCommandArgument) {
        popLiteralPosition();
      }
    }
  }

  void printFirstArguments(Executable executable) throws IOException;

  private void printExecutesArguments(final Executable executable) throws IOException {
    final String argumentsTypeGetter = executable instanceof DefaultExecutable def ? def.defaultExecutableArgumentTypes().getGetter() : null;
    final List<CommandArgument> arguments = executable.parameterArguments();

    printFirstArguments(executable);
    if (arguments.isEmpty() && argumentsTypeGetter == null) {
      println();
      return;
    }

    print(",");
    println();

    if (arguments.isEmpty() && argumentsTypeGetter != null) {
      println(argumentsTypeGetter);
      return;
    }

    printArguments(arguments);

    for (final CommandArgument argument : arguments) {
      if (argument instanceof MultiLiteralCommandArgument) {
        popLiteralPosition();
      }
    }

    if (argumentsTypeGetter != null) {
      print(argumentsTypeGetter);
      println();
    }
  }

  private void printArguments(final List<? extends CommandArgument> arguments) throws IOException {
    for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
      final CommandArgument argument = arguments.get(i);

      printIndent();
      print(switch (argument) {
        case RequiredCommandArgument req -> req.argumentType().retriever();
        case MultiLiteralCommandArgument ignored -> '"' + nextLiteral() + '"';
        case LiteralCommandArgument lit -> '"' + lit.literal() + '"';
        default -> throw new IllegalArgumentException("Unknown argument class: " + argument.getClass());
      });

      if (i + 1 < argumentsSize) {
        print(",");
      }

      println();
    }
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
  }
}
