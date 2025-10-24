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

import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.CommandInformation;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public abstract class CommonCommandTreePrinter<C extends CommandInformation> extends AbstractPrinter implements PrinterInformation<C>, ImportPrinter<C>, InstanceFieldPrinter<C>, TreePrinter<C> {
  private final Stack<String> multiLiteralStack = new Stack<>();
  private final Stack<ExecuteAccess<?>> executeAccessStack = new Stack<>();
  protected final CommandNode node;
  private final Set<String> printedInstances = new TreeSet<>();
  private final ProcessingEnvironment environment;

  private int multiLiteralStackPosition = 0;

  private final C commandInformation;

  public CommonCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final C commandInformation,
      final ProcessingEnvironment environment
  ) {
    super(indent, writer);
    this.node = node;
    this.commandInformation = commandInformation;
    this.environment = environment;
  }

  public String getPackageName() {
    return commandInformation.sourceClass().getPackageName();
  }

  public String getBrigadierClassName() {
    return commandInformation.sourceClass().getName() + "Brigader";
  }

  public abstract void print() throws IOException;

  @Override
  public String nextLiteral() {
    return this.multiLiteralStack.elementAt(this.multiLiteralStackPosition++);
  }

  @Override
  public void pushLiteral(final String literal) {
    this.multiLiteralStack.push(literal);
  }

  @Override
  public void popLiteral() {
    this.multiLiteralStack.pop();
  }

  @Override
  public ProcessingEnvironment environment() {
    return this.environment;
  }

  @Override
  public void popLiteralPosition() {
    this.multiLiteralStackPosition--;
  }

  @Override
  public Set<String> getPrintedInstances() {
    return printedInstances;
  }

  @Override
  public Stack<ExecuteAccess<?>> getAccessStack() {
    return executeAccessStack;
  }

  @Override
  public CommandNode getNode() {
    return node;
  }

  @Override
  public C getCommandInformation() {
    return this.commandInformation;
  }
}
