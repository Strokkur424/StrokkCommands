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

import net.strokkur.commands.internal.BuildConstants;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.internal.util.PrintParamsHolder;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public abstract class CommonCommandTreePrinter<C extends CommandInformation> extends AbstractPrinter implements PrinterInformation<C>, ImportPrinter<C>, InstanceFieldPrinter<C>, TreePrinter<C> {
  private final Stack<String> multiLiteralStack = new Stack<>();
  private final Stack<ExecuteAccess<?>> executeAccessStack = new Stack<>();
  protected final CommandNode node;
  private final Set<String> printedInstances = new TreeSet<>();
  private final ProcessingEnvironment environment;
  protected final PlatformUtils utils;

  private int multiLiteralStackPosition = 0;

  private final C commandInformation;

  public CommonCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final C commandInformation,
      final ProcessingEnvironment environment,
      final PlatformUtils utils
  ) {
    super(indent, writer);
    this.node = node;
    this.commandInformation = commandInformation;
    this.environment = environment;
    this.utils = utils;
  }

  public final String getPackageName() {
    return commandInformation.sourceClass().getPackageName();
  }

  public final String getBrigadierClassName() {
    return commandInformation.sourceClass().getName() + "Brigadier";
  }

  protected abstract PrintParamsHolder getParamsHolder();

  protected abstract void printRegisterMethod(final PrintParamsHolder holder) throws IOException;

  protected void printSemicolon() throws IOException {
    print(";");
  }

  public void print() throws IOException {
    final String packageName = getPackageName();
    final PrintParamsHolder printParams = getParamsHolder();

    println("package {};", packageName);
    println();
    printImports(getImports());
    println();

    printBlock("""
            /**
             * A class holding the Brigadier source tree generated from
             * {@link %s} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
             *
             * @author Strokkur24 - StrokkCommands
             * @version %s
             * @see #create(%s) creating the %s
             * @see #register(%s) registering the command
             */
            @NullMarked""",
        getCommandInformation().sourceClass().getName(),
        BuildConstants.VERSION,
        printParams.createJdParams(),
        utils.getNodeReturnType(),
        printParams.registerJdParams()
    );

    println("public final class {} {", getBrigadierClassName());
    incrementIndent();
    println();

    printRegisterMethod(printParams);

    println();

    printBlock("""
            /**
             * A method for creating a Brigadier command node which denotes the declared command
             * in {@link %s}. You can either retrieve the unregistered node with this method
             * or register it directly with {@link #register(%s)}.
             */
            public static%s %s<%s> create(%s) {""",
        getCommandInformation().sourceClass().getName(),
        printParams.registerJdParams(),
        Optional.ofNullable(getCommandInformation().constructor())
            .map(SourceMethod::getCombinedTypeAnnotationsString)
            .orElse(""),
        utils.getNodeReturnType(),
        List.of(utils.platformType().split("\\.")).getLast(),
        printParams.createParams()
    );
    incrementIndent();

    printInstanceFields();

    printIndent();
    print("return ");
    incrementIndent();
    printNode(node);
    printSemicolon();

    println();
    decrementIndent();
    decrementIndent();
    println("}");
    println();

    printBlock("""
            /**
             * The constructor is not accessible. There is no need for an instance
             * to be created, as no state is stored and all methods are static.
             *
             * @throws IllegalAccessException always
             */
            private %s() throws IllegalAccessException {
                throw new IllegalAccessException("This class cannot be instantiated.");
            }""",
        getBrigadierClassName());
    decrementIndent();
    println("}");
  }

  @Override
  public final String nextLiteral() {
    return this.multiLiteralStack.elementAt(this.multiLiteralStackPosition++);
  }

  @Override
  public final void pushLiteral(final String literal) {
    this.multiLiteralStack.push(literal);
  }

  @Override
  public final void popLiteral() {
    this.multiLiteralStack.pop();
  }

  @Override
  public final ProcessingEnvironment environment() {
    return this.environment;
  }

  @Override
  public final void popLiteralPosition() {
    this.multiLiteralStackPosition--;
  }

  @Override
  public final Set<String> getPrintedInstances() {
    return printedInstances;
  }

  @Override
  public final Stack<ExecuteAccess<?>> getAccessStack() {
    return executeAccessStack;
  }

  @Override
  public final CommandNode getNode() {
    return node;
  }

  @Override
  public final C getCommandInformation() {
    return this.commandInformation;
  }
}
