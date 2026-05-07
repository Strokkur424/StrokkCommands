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
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.ParameterType;
import net.strokkur.commands.internal.intermediate.executable.Parameterizable;
import net.strokkur.commands.internal.intermediate.executable.SourceParameterType;
import net.strokkur.commands.internal.intermediate.registrable.ExecutorWrapperProvider;
import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionProvider;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.IOExceptionIgnoringConsumer;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public abstract class CommonTreePrinter {
  protected final CommonCommandTreePrinter<?> printer;
  private final Stack<String> multiLiteralStack = new Stack<>();

  private final Map<DefaultExecutable, String> stackAtDefaultExecutes = new HashMap<>();

  private int multiLiteralStackPosition = 0;

  public CommonTreePrinter(CommonCommandTreePrinter<?> printer) {
    this.printer = printer;
  }

  public final String nextLiteral() {
    return this.multiLiteralStack.elementAt(this.multiLiteralStackPosition++);
  }

  public final void pushLiteral(String literal) {
    this.multiLiteralStack.push(literal);
  }

  public final void popLiteral() {
    this.multiLiteralStack.pop();
  }

  public final void popLiteralPosition() {
    this.multiLiteralStackPosition--;
  }

  protected abstract void prefixPrintExecutableInner(CommandNode node, Executable executable) throws IOException;

  protected abstract String handleParameter(SourceVariable parameter) throws IOException;

  public void printNode(CommandNode node) throws IOException {
    printNode(node, false);
  }

  private void printNode(CommandNode root, boolean isNested) throws IOException {
    printForArguments(root, initializer -> {
      if (isNested) {
        printer.println();
        printer.printIndented(".then(");
        printer.incrementIndent();
      }

      printer.print(initializer);

      if (root.argument() instanceof RequiredCommandArgument req && req.hasAttribute(AttributeKey.SUGGESTION_PROVIDER)) {
        final SuggestionProvider provider = req.getAttributeNotNull(AttributeKey.SUGGESTION_PROVIDER);
        printer.println();
        printer.printIndented(".suggests(" + provider.getSuggestionString() + ")");
      }

      final RequirementProvider requirementProvider = root.getAttribute(AttributeKey.REQUIREMENT_PROVIDER);
      final String extraRequirement = getExtraRequirements(root);

      if (requirementProvider != null && extraRequirement == null) {
        printer.println();
        printer.printIndented(".requires(source -> %s)", requirementProvider.getRequirementString());
      } else if (requirementProvider == null && extraRequirement != null) {
        printer.println();
        printer.printIndented(".requires(source -> %s)", extraRequirement);
      } else if (requirementProvider != null) {
        printer.println();
        printer.printIndented(".requires(source -> %s && %s)", requirementProvider.getRequirementString(), extraRequirement);
      }

      final Executable executable = root.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
      if (executable != null) {
        printExecutableInner(root, executable);
      }

      for (CommandNode node : root.children()) {
        printNode(node, true);
      }

      if (isNested) {
        printer.decrementIndent();
        printer.println();
        printer.printIndented(")");
      }
    }, isNested);
  }

  private void printExecutableInner(CommandNode node, Executable executable) throws IOException {
    printer.println();

    final ExecutorWrapperProvider wrapper = node.getAttributeOr(AttributeKey.EXECUTOR_WRAPPER, printer.getExecutorWrapper());
    if (wrapper == null) {
      printExecutableInnerNoWrapper(node, executable);
    } else {
      printExecutableInnerWithWrapper(node, executable, wrapper);
    }
  }

  private void printExecutableInnerNoWrapper(CommandNode node, Executable executable) throws IOException {
    printer.println(".executes(ctx -> {");
    printer.incrementIndent();
    prefixPrintExecutableInner(node, executable);
    printExecutableBody(node, executable);
    printer.println("return Command.SINGLE_SUCCESS;");
    printer.decrementIndent();
    printer.printIndented("})");
  }

  private void printExecutableInnerWithWrapper(CommandNode node, Executable executable, ExecutorWrapperProvider wrapper) throws IOException {
    printer.println(".executes(%s(ctx -> {", getWrapperMethodCall(wrapper));
    printer.incrementIndent();
    prefixPrintExecutableInner(node, executable);
    printExecutableBody(node, executable);
    printer.println("return Command.SINGLE_SUCCESS;");
    printer.decrementIndent();

    if (wrapper.wrapperType().withMethod()) {
      final List<SourceParameter> params = executable.executesMethod().getParameters();
      final String parameterTypesString = params.isEmpty() ? "" : ", " + String.join(", ", executable.executesMethod().getParameters().stream()
          .map(SourceParameter::getType)
          .map(SourceType::getSourceName)
          .map((str) -> str + ".class")
          .toList());

      printer.printIndented("}, getMethodViaReflection(%s.class, \"%s\"%s)))",
          executable.executesMethod().getEnclosed().getSourceName(),
          executable.executesMethod().getName(),
          parameterTypesString
      );
    } else {
      printer.printIndented("}))");
    }
  }

  private String getWrapperMethodCall(ExecutorWrapperProvider wrapper) {
    final SourceMethod method = wrapper.wrapperMethod();
    final String methodName = method.getName();

    if (wrapper.wrapperMethod().isStaticallyAccessible()) {
      // Static method: WrapperClass.wrapperMethod
      final SourceClass wrapperClass = wrapper.wrapperMethod().getEnclosed();
      return wrapperClass.getSourceName() + "." + methodName;
    } else {
      // Instance method: get the appropriate instance variable name
      return Utils.getInstanceName(printer.getExecutorWrapperAccessStack()) + "." + methodName;
    }
  }

  private void printExecutableBody(CommandNode node, Executable executable) throws IOException {
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

  private void printWithInstance(Executable executable) throws IOException {
    final String instanceName;
    if (executable instanceof DefaultExecutable defaultExec) {
      instanceName = stackAtDefaultExecutes.computeIfAbsent(defaultExec, unused -> Utils.getInstanceName(printer.getAccessStack()));
    } else {
      instanceName = Utils.getInstanceName(printer.getAccessStack());
    }
    printExecutesMethodCall(executable, instanceName);
  }

  private void printExecutesMethodCall(Executable executable, String typeName) throws IOException {
    printer.printIndented("{}.{}", typeName, executable.executesMethod().getName());
    printExecutesArguments(executable);
    printer.print(";");
    printer.println();
  }

  private void printWithRecord(Parameterizable record, Executable executable) throws IOException {
    final String typeName = executable.executesMethod().getEnclosed().getSourceName();

    if (record.parameterArguments().isEmpty()) {
      printer.println("final {} executorClass = new {}();", typeName, typeName);
    } else {
      printer.println("final {} executorClass = new {}(", typeName, typeName);
      printer.incrementIndent();
      printArguments(record.parameterArguments().stream()
          .map(CommandArgument.class::cast)
          .toList());
      printer.decrementIndent();
      printer.println(");");
    }

    printExecutesMethodCall(executable, "executorClass");

    for (ParameterType arg : record.parameterArguments()) {
      if (arg instanceof MultiLiteralCommandArgument) {
        popLiteralPosition();
      }
    }
  }

  private void printExecutesArguments(Executable executable) throws IOException {
    final List<ParameterType> parameterArguments = executable.parameterArguments();
    if (parameterArguments.isEmpty()) {
      printer.print("()");
      return;
    }

    if (parameterArguments.size() == 1) {
      printer.print("(");
      switch (parameterArguments.getFirst()) {
        case CommandArgument argument -> printArgument(argument);
        case SourceParameterType(SourceVariable parameter) -> printer.print(handleParameter(parameter));
      }
      printer.print(")");
      return;
    }

    printer.print("(");
    printer.incrementIndent();
    printer.println();
    for (int i = 0, parameterArgumentsSize = parameterArguments.size(); i < parameterArgumentsSize; i++) {
      final ParameterType parameterArgument = parameterArguments.get(i);
      printer.printIndent();

      switch (parameterArgument) {
        case CommandArgument argument -> printArgument(argument);
        case SourceParameterType(SourceVariable parameter) -> printer.print(handleParameter(parameter));
      }

      if (i + 1 < parameterArgumentsSize) {
        printer.print(",");
        printer.println();
      }
    }

    for (ParameterType argument : parameterArguments) {
      if (argument instanceof MultiLiteralCommandArgument) {
        popLiteralPosition();
      }
    }

    printer.decrementIndent();
    printer.println();
    printer.printIndented(")");
  }

  private void printArguments(List<CommandArgument> arguments) throws IOException {
    for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
      printer.printIndent();
      printArgument(arguments.get(i));

      if (i + 1 < argumentsSize) {
        printer.print(",");
      }

      printer.println();
    }
  }

  private void printArgument(CommandArgument argument) throws IOException {
    printer.print(switch (argument) {
      case RequiredCommandArgument req -> req.argumentType().retriever();
      case MultiLiteralCommandArgument ignored -> '"' + nextLiteral() + '"';
      case LiteralCommandArgument lit -> '"' + lit.literal() + '"';
      default -> throw new IllegalArgumentException("Unknown argument class: " + argument.getClass());
    });
  }

  @Nullable
  protected abstract String getExtraRequirements(Attributable node);

  protected abstract String getLiteralMethodString();

  protected abstract String getArgumentMethodString();

  public String getCommandNameLiteralOverride(LiteralCommandArgument lit) {
    return "NAME";
  }

  private void printForArguments(CommandNode node, IOExceptionIgnoringConsumer<String> initializer, boolean isNested) throws IOException {
    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      node.getAttributeNotNull(AttributeKey.ACCESS_STACK).forEach(printer.getAccessStack()::push);
    }

    final ExecutorWrapperProvider current = printer.getExecutorWrapper();

    // TODO: this bleeds over if a method has a wrapper declared and a method is present which starts with the same args and merges.
    // However, this is a cheap fix for a problem barely anyone will run into anyways and which can be solved with just one
    // UnsetExecutorWrapper annotation. Also it is 2:30 AM, cut me some slack.
    final ExecutorWrapperProvider executorWrapper = node.getAttribute(AttributeKey.EXECUTOR_WRAPPER);
    if (node.getAttributeNotNull(AttributeKey.EXECUTOR_WRAPPER_UNSET)) {
      printer.updateExecutorWrapper(null);
    } else if (executorWrapper != null) {
      printer.updateExecutorWrapper(executorWrapper);
    }

    switch (node.argument()) {
      case LiteralCommandArgument lit -> initializer.accept("%s(%s)".formatted(getLiteralMethodString(), isNested ? '"' + lit.literal() + '"' : getCommandNameLiteralOverride(lit)));
      case RequiredCommandArgument req -> initializer.accept("%s(\"%s\", %s)".formatted(getArgumentMethodString(), req.argumentName(), req.argumentType().initializer()));
      case MultiLiteralCommandArgument multi -> {
        for (String literal : multi.literals()) {
          pushLiteral(literal);
          initializer.accept("%s(\"%s\")".formatted(getLiteralMethodString(), literal));
          popLiteral();
        }
      }
      default -> throw new IllegalArgumentException("Unknown argument class: " + node.argument().getClass());
    }

    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      node.getAttributeNotNull(AttributeKey.ACCESS_STACK).forEach(access -> printer.getAccessStack().pop());
    }
    printer.updateExecutorWrapper(current);
  }
}
