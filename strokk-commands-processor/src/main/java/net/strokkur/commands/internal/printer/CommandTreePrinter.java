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
import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public final class CommandTreePrinter extends AbstractPrinter implements PrinterInformation, ImportPrinter, InstanceFieldPrinter, PathPrinter {

  private final Stack<String> multiLiteralStack = new Stack<>();
  private final Stack<ExecuteAccess<?>> executeAccessStack = new Stack<>();
  private final CommandNode node;
  private final CommandInformation commandInformation;
  private final Set<String> printedInstances = new TreeSet<>();

  private int multiLiteralStackPosition = 0;

  public CommandTreePrinter(final int indent, final @Nullable Writer writer, final CommandNode node, final CommandInformation commandInformation) {
    super(indent, writer);
    this.node = node;
    this.commandInformation = commandInformation;
  }

  public String getPackageName() {
    return ((PackageElement) commandInformation.classElement().getEnclosingElement()).getQualifiedName().toString();
  }

  public String getBrigadierClassName() {
    return commandInformation.classElement().getSimpleName().toString() + "Brigadier";
  }

  public void print() throws IOException {
    final String packageName = getPackageName();
    final String brigadierClassName = getBrigadierClassName();

    final String description = commandInformation.description() == null ? "null" : '"' + commandInformation.description() + '"';
    final String aliases = commandInformation.aliases() == null ? "" : '"' + String.join("\", \"", List.of(commandInformation.aliases())) + '"';

    println("package {};", packageName);
    println();
    printImports(getImports());
    println();

    printBlock("""
            /**
             * A class holding the Brigadier source tree generated from
             * {@link {}} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
             *
             * @author Strokkur24 - StrokkCommands
             * @version {}
             * @see #create() Creating the LiteralArgumentNode.
             * @see #register(Commands) Registering the command.
             */
            @NullMarked""",
        commandInformation.classElement().getSimpleName().toString(),
        BuildConstants.VERSION
    );

    println("public final class {} {", brigadierClassName);
    incrementIndent();

    final String constructorTypeParameters = this.commandInformation.constructor() == null
        ? ""
        : Utils.getMethodTypeParameterString(this.commandInformation.constructor());

    final List<String> createParameters = this.commandInformation.constructor() == null
        ? new ArrayList<>()
        : Utils.getParameterStrings(this.commandInformation.constructor().getParameters());
    final List<String> registerParameters = new ArrayList<>(createParameters.size() + 1);
    registerParameters.add("Commands commands");
    registerParameters.addAll(createParameters);

    final String parameterTypes = this.commandInformation.constructor() == null
        ? ""
        : Utils.getParameterTypes(this.commandInformation.constructor().getParameters());

    println();
    printBlock("""
            /**
             * Shortcut for registering the command node returned from
             * {@link #create({})}. This method uses the provided aliases
             * and description from the original source file.
             * <p>
             * <h3>Registering the command</h3>
             * <p>
             * This method can safely be called either in your plugin bootstrapper's
             * {@link io.papermc.paper.plugin.bootstrap.PluginBootstrap#bootstrap(io.papermc.paper.plugin.bootstrap.BootstrapContext)}, your main
             * class' {@link org.bukkit.plugin.java.JavaPlugin#onLoad()} or {@link org.bukkit.plugin.java.JavaPlugin#onEnable()}
             * method.
             * <p>
             * You need to call it inside of a lifecycle event. General information can be found on the
             * <a href="https://docs.papermc.io/paper/dev/lifecycle/">PaperMC Lifecycle API docs page</a>.
             * The general use case might look like this (example given inside the {@code onEnable} method):
             * <p>
             * <pre>{@code
             * this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
             *     final Commands commands = event.registrar();
             *     {}.register(commands);
             * }
             * }</pre>
             */
            public static{} void register({}) {
                commands.register(create({}), {}, List.of({}));
            }""",
        parameterTypes,
        brigadierClassName,
        constructorTypeParameters,
        String.join(", ", registerParameters),
        String.join(", ", getCommandInformation().constructor() instanceof ExecutableElement ctor
            ? ctor.getParameters().stream()
            .map(var -> var.getSimpleName().toString())
            .toList()
            : Collections.emptyList()
        ),
        description,
        aliases
    );

    println();

    printBlock("""
            /**
             * A method for creating a Brigadier command node which denotes the declared command
             * in {@link {}}. You can either retrieve the unregistered node with this method
             * or register it directly with {@link #register({})}.
             */
            public static{} LiteralCommandNode<CommandSourceStack> create({}) {""",
        commandInformation.classElement().getSimpleName().toString(),
        (parameterTypes.isBlank() ? "Commands" : "Commands, " + parameterTypes),
        constructorTypeParameters,
        String.join(", ", createParameters)
    );
    incrementIndent();

    printInstanceFields();

    printIndent();
    print("return ");
    incrementIndent();
    printNode(node);
    println(".build();");
    decrementIndent();
    decrementIndent();
    println("}");
    println();

    printBlock("""
            /**
             * The constructor is not accessible. There is no need for an instance
             * to be created, as no state is stored, and all methods are static.
             *
             * @throws IllegalAccessException
             */
            private {}() throws IllegalAccessException {
                throw new IllegalAccessException("Cannot create instance of static class.");
            }
            """,
        brigadierClassName);

    decrementIndent();
    println("}");
  }

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
  public CommandInformation getCommandInformation() {
    return commandInformation;
  }
}
