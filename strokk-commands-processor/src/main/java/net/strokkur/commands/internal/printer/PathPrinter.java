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
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.attributes.Parameterizable;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.requirement.Requirement;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

interface PathPrinter extends Printable, PrinterInformation {

  default void printPath(CommandPath<?> path) throws IOException {
    final Executable executable = path.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      printExecutablePath(path, executable);
      return;
    }

    printGenericPath(path, () -> {});
  }

  private void printExecutablePath(CommandPath<?> path, Executable executable) throws IOException {
    this.printGenericPath(path, () -> {
      // print the .executes method
      println();
      println(".executes(ctx -> {");
      incrementIndent();

      final ExecutorType executorType = executable.executorType();
      if (executorType != ExecutorType.NONE) {
        printBlock("""
                if (!(ctx.getSource().getExecutor() instanceof {} executor)) {
                    throw new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                        Component.text("This command requires {} {} executor!")
                    )).create();
                }""",
            executorType.toString().charAt(0) + executorType.toString().toLowerCase().substring(1),
            executorType == ExecutorType.ENTITY ? "an" : "a",
            executorType.toString().toLowerCase()
        );
        println();
      }

      boolean instancePrint = true;
      CommandPath<?> parentPath = path;

      while ((parentPath = parentPath.getParent()) != null) {
        final Parameterizable recordArguments = parentPath.getAttribute(AttributeKey.RECORD_ARGUMENTS);
        if (recordArguments != null) { // TODO: THIS IS NEVER CALLED. FIX THIS.
          printWithRecord(recordArguments, executable);
          instancePrint = false;
          break;
        }
      }

      if (instancePrint) {
        printWithInstance(executable);
      }

      println("return Command.SINGLE_SUCCESS;");
      decrementIndent();
      println("})");
    });
  }

  private void printWithInstance(Executable executable) throws IOException {
    final List<ExecuteAccess<?>> pathToUse;
    if (getAccessStack().size() > 1 && getAccessStack().reversed().get(1) instanceof FieldAccess) {
      pathToUse = getAccessStack().subList(0, getAccessStack().size() - 1);
    } else {
      pathToUse = getAccessStack();
    }

    printExecutesMethodCall(executable, Utils.getInstanceName(pathToUse));
  }

  private void printExecutesMethodCall(Executable executable, String typeName) throws IOException {
    println("{}.{}(", typeName, executable.executesMethod().getSimpleName().toString());
    incrementIndent();

    // Arguments
    printExecutesArguments(executable);

    decrementIndent();
    println(");");
  }

  private void printWithRecord(Parameterizable record, Executable executable) throws IOException {
    final String typeName = Utils.getTypeName(executable.executesMethod().getEnclosingElement());

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
  }

  private void printExecutesArguments(Executable executable) throws IOException {
    final ExecutorType executorType = executable.executorType();
    final String argumentsTypeGetter = executable instanceof DefaultExecutable def ? def.defaultExecutableArgumentTypes().getGetter() : null;
    final List<? extends CommandArgument> arguments = executable.parameterArguments();

    if (arguments.isEmpty() && executorType == ExecutorType.NONE && argumentsTypeGetter == null) {
      println("ctx.getSource().getSender()");
      return;
    }

    println("ctx.getSource().getSender(),");
    if (executorType != ExecutorType.NONE) {
      printIndent();
      switch (executorType) {
        case ENTITY, PLAYER -> print("executor");
      }

      if (arguments.isEmpty() && argumentsTypeGetter == null) {
        println();
        return;
      }

      print(",");
      println();
    }

    if (arguments.isEmpty() && argumentsTypeGetter != null) {
      println(argumentsTypeGetter);
      return;
    }

    printArguments(arguments);

    if (argumentsTypeGetter != null) {
      print(argumentsTypeGetter);
      println();
    }
  }

  private void printArguments(List<? extends CommandArgument> arguments) throws IOException {
    for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
      final CommandArgument argument = arguments.get(i);

      printIndent();
      if (argument instanceof RequiredCommandArgument requiredArgument) {
        print(requiredArgument.getArgumentType().retriever());
      } else {
        print("\"{}\"", argument.getName());
      }

      if (i + 1 < argumentsSize) {
        print(",");
      }

      println();
    }
  }

  private void printArgument(CommandArgument argument) throws IOException {
    switch (argument) {
      case LiteralCommandArgument literalArgument -> printLiteral(literalArgument);
      case RequiredCommandArgument requiredArgument -> printRequiredArg(requiredArgument);
      default -> throw new IllegalStateException("Unknown argument: " + argument);
    }
  }

  private void printLiteral(LiteralCommandArgument literalArg) throws IOException {
    print("Commands.literal(\"{}\")", literalArg.literal());
  }

  private void printRequiredArg(RequiredCommandArgument requiredArg) throws IOException {
    print("Commands.argument(\"{}\", {})",
        requiredArg.getName(),
        requiredArg.getArgumentType().initializer()
    );

    if (requiredArg.getSuggestionProvider() != null) {
      incrementIndent();
      println();
      printIndent();
      print(".suggests(" + requiredArg.getSuggestionProvider().getProvider() + ")");
      decrementIndent();
    }
  }

  private void printRequires(@Nullable CommandPath<?> path) throws IOException {
    if (path != null) {
      final List<Requirement> requirements = new ArrayList<>();

      final boolean operator = path.getAttributeNotNull(AttributeKey.REQUIRES_OP);
      final ExecutorType executorType;

      if (path.hasAttribute(AttributeKey.EXECUTOR_TYPE) && !path.getAttributeNotNull(AttributeKey.EXECUTOR_HANDLED)) {
        executorType = path.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);
      } else {
        executorType = ExecutorType.NONE;
      }

      final Requirement req = path.getAttribute(AttributeKey.REQUIREMENT);
      if (req != null) {
        requirements.add(req);
      }

      requirements.addAll(path.getAttributeNotNull(AttributeKey.PERMISSIONS)
          .stream()
          .map(Requirement::permission)
          .toList());

      if (!requirements.isEmpty()) {
        final String requirementString = Requirement.combine(requirements).getRequirementString(operator, executorType);
        if (!requirementString.isEmpty()) {
          println();
          printIndent();
          print(".requires(source -> {})", requirementString);
        }
      }
    }
  }

  private <T extends CommandArgument> void printArguments(List<T> arguments, @Nullable CommandPath<?> printRequirements, InsidePrinter insidePrinter, boolean noStartingThen) throws IOException {
    if (arguments.isEmpty()) {
      insidePrinter.print();
      return;
    }

    T arg = arguments.removeFirst();
    if (!noStartingThen) {
      println();
      printIndent();
      print(".then(");
    }

    printArgument(arg);
    incrementIndent();

    printRequires(printRequirements);
    printArguments(arguments, null, insidePrinter, false);
    decrementIndent();

    if (!noStartingThen) {
      println(")");
    }
  }

  private <T extends CommandArgument> void printGenericPath(CommandPath<T> path, InsidePrinter insidePrinter) throws IOException {
    printArguments(new ArrayList<>(path.getArguments()), path, () -> {
      if (path.hasAttribute(AttributeKey.ACCESS_STACK)) {
        path.getAttributeNotNull(AttributeKey.ACCESS_STACK).forEach(getAccessStack()::push);
      }

      for (final CommandPath<?> child : path.getChildren()) {
        printPath(child);
      }

      insidePrinter.print();
      if (path.hasAttribute(AttributeKey.ACCESS_STACK)) {
        path.getAttributeNotNull(AttributeKey.ACCESS_STACK).forEach(
            access -> getAccessStack().pop()
        );
      }
    }, path.getParent() == null);
  }

  @FunctionalInterface
  interface InsidePrinter {
    void print() throws IOException;
  }
}
