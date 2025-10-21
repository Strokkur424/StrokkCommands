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
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.attributes.Parameterizable;
import net.strokkur.commands.internal.intermediate.requirement.Requirement;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.IOExceptionIgnoringConsumer;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

interface TreePrinter extends Printable, PrinterInformation {

  String nextLiteral();

  void pushLiteral(String literal);

  void popLiteral();

  void popLiteralPosition();

  default void printNode(final CommandNode node) throws IOException {
    printNode(node, false);
  }

  private void printNode(final CommandNode root, boolean printThen) throws IOException {
    printForArguments(root, initializer -> {
      if (printThen) {
        println();
        printIndent();
        print(".then(");
        incrementIndent();
      }

      print(initializer);

      if (root.argument() instanceof RequiredCommandArgument req) {
        if (req.getSuggestionProvider() != null) {
          println();
          printIndent();
          print(".suggests(" + req.getSuggestionProvider().getProvider() + ")");
        }
      }

      printRequires(root);

      final Executable executable = root.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
      if (executable != null) {
        printExecutableInner(root, executable);
      }

      for (final CommandNode node : root.children()) {
        printNode(node, true);
      }

      if (printThen) {
        decrementIndent();
        println(")");
      }
    });
  }

  private void printExecutableInner(final CommandNode node, final Executable executable) throws IOException {
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

    println("return Command.SINGLE_SUCCESS;");
    decrementIndent();
    println("})");
  }

  private void printWithInstance(final Executable executable) throws IOException {
    final List<ExecuteAccess<?>> pathToUse;
    if (getAccessStack().size() > 1 && getAccessStack().reversed().get(1) instanceof FieldAccess) {
      pathToUse = getAccessStack().subList(0, getAccessStack().size() - 1);
    } else {
      pathToUse = getAccessStack();
    }

    printExecutesMethodCall(executable, Utils.getInstanceName(pathToUse));
  }

  private void printExecutesMethodCall(final Executable executable, final String typeName) throws IOException {
    println("{}.{}(", typeName, executable.executesMethod().getSimpleName().toString());
    incrementIndent();

    // Arguments
    printExecutesArguments(executable);

    decrementIndent();
    println(");");
  }

  private void printWithRecord(final Parameterizable record, final Executable executable) throws IOException {
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

    for (final CommandArgument arguments : record.parameterArguments()) {
      if (arguments instanceof MultiLiteralCommandArgument) {
        popLiteralPosition();
      }
    }
  }

  private void printExecutesArguments(final Executable executable) throws IOException {
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
        case RequiredCommandArgument req -> req.getArgumentType().retriever();
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

  private void printRequires(@Nullable Attributable attributable) throws IOException {
    if (attributable != null) {
      final List<Requirement> requirements = new ArrayList<>();

      final boolean operator = attributable.getAttributeNotNull(AttributeKey.REQUIRES_OP);

      final ExecutorType executorType;
      if (attributable.hasAttribute(AttributeKey.EXECUTOR_TYPE)) {
        executorType = attributable.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);
      } else {
        executorType = ExecutorType.NONE;
      }

      final Requirement req = attributable.getAttribute(AttributeKey.REQUIREMENT);
      if (req != null) {
        requirements.add(req);
      }

      requirements.addAll(attributable.getAttributeNotNull(AttributeKey.PERMISSIONS)
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

  private void printForArguments(final CommandNode node, final IOExceptionIgnoringConsumer<String> initializer) throws IOException {
    if (node.hasAttribute(AttributeKey.ACCESS_STACK)) {
      node.getAttributeNotNull(AttributeKey.ACCESS_STACK).forEach(getAccessStack()::push);
    }

    switch (node.argument()) {
      case LiteralCommandArgument lit -> initializer.accept("Commands.literal(\"%s\")".formatted(lit.literal()));
      case RequiredCommandArgument req -> initializer.accept("Commands.argument(\"%s\", %s)".formatted(req.getName(), req.getArgumentType().initializer()));
      case MultiLiteralCommandArgument multi -> {
        for (final String literal : multi.literals()) {
          pushLiteral(literal);
          initializer.accept("Commands.literal(\"%s\")".formatted(literal));
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
