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
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceType;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.attributes.PaperAttributeKeys;
import net.strokkur.commands.internal.intermediate.requirement.Requirement;
import net.strokkur.commands.internal.intermediate.suggestions.SuggestionProvider;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.PaperClasses;
import net.strokkur.commands.internal.util.PaperCommandInformation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@NullMarked
public class PaperCommandTreePrinter extends CommonCommandTreePrinter<PaperCommandInformation> {

  public PaperCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final PaperCommandInformation commandInformation,
      final ProcessingEnvironment environment
  ) {
    super(indent, writer, node, commandInformation, environment);
  }

  @Override
  public void print() throws IOException {
    final String packageName = getPackageName();
    final String brigadierClassName = getBrigadierClassName();

    final String description = getCommandInformation().description() == null ? "null" : '"' + getCommandInformation().description() + '"';
    final String aliases = getCommandInformation().aliases() == null ? "" : '"' + String.join("\", \"", List.of(getCommandInformation().aliases())) + '"';

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
        getCommandInformation().sourceClass().getName(),
        BuildConstants.VERSION
    );

    println("public final class {} {", brigadierClassName);
    incrementIndent();

    final String constructorTypeParameters = this.getCommandInformation().constructor() == null
        ? ""
        : this.getCommandInformation().constructor().getTypeAnnotationsString();

    final List<String> createParameters = this.getCommandInformation().constructor() == null
        ? new ArrayList<>()
        : this.getCommandInformation().constructor().getParameters().stream().map(SourceParameter::getFullDefinition).toList();
    final List<String> registerParameters = new ArrayList<>(createParameters.size() + 1);
    registerParameters.add("Commands commands");
    registerParameters.addAll(createParameters);

    final String parameterTypes = this.getCommandInformation().constructor() == null
        ? ""
        : String.join(", ", this.getCommandInformation().constructor().getParameters().stream().map(SourceParameter::getType).map(SourceType::getName).toList());

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
        getCommandInformation().sourceClass().getName(),
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
  public Set<String> standardImports() {
    return Set.of(
        Classes.COMMAND,
        Classes.LITERAL_COMMAND_NODE,
        PaperClasses.COMMAND_SOURCE_STACK,
        PaperClasses.COMMANDS,
        Classes.LIST,
        Classes.NULL_MARKED
    );
  }

  @Override
  public void gatherAdditionalArgumentImports(final Set<String> imports, final RequiredCommandArgument argument) {
    if (!argument.hasAttribute(PaperAttributeKeys.SUGGESTION_PROVIDER)) {
      return;
    }

    final SuggestionProvider provider = argument.getAttributeNotNull(PaperAttributeKeys.SUGGESTION_PROVIDER);
    imports.addAll(provider.getClassElement().getImports());
  }

  @Override
  public void gatherAdditionalNodeImports(final Set<String> imports, final CommandNode node) {
    addExecutorTypeImports(imports, node.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE));
    final Executable executable = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      addExecutorTypeImports(imports, executable.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE));
    }
  }

  private void addExecutorTypeImports(final Set<String> imports, final ExecutorType type) {
    if (type == ExecutorType.NONE) {
      return;
    }

    if (type == ExecutorType.PLAYER) {
      imports.add(PaperClasses.PLAYER);
    } else if (type == ExecutorType.ENTITY) {
      imports.add(PaperClasses.ENTITY);
    }

    imports.add(Classes.SIMPLE_COMMAND_EXCEPTION_TYPE);
    imports.add(PaperClasses.MESSAGE_COMPONENT_SERIALIZER);
    imports.add(PaperClasses.COMPONENT);
  }

  @Override
  public void printAdditionalNodesData(final RequiredCommandArgument req) throws IOException {
    if (!req.hasAttribute(PaperAttributeKeys.SUGGESTION_PROVIDER)) {
      return;
    }

    final SuggestionProvider provider = req.getAttributeNotNull(PaperAttributeKeys.SUGGESTION_PROVIDER);
    println();
    printIndent();
    print(".suggests(" + provider.getProvider() + ")");
  }

  @Override
  public void prefixPrintExecutableInner(final CommandNode node, final Executable executable) throws IOException {
    final ExecutorType executorType = executable.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);
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
  }

  @Override
  public void printFirstArguments(final Executable executable) throws IOException {
    final ExecutorType executorType = executable.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);

    printIndent();
    print("ctx.getSource().getSender()");
    if (executorType == ExecutorType.NONE) {
      return;
    }

    println();
    printIndent();
    print("executor");
  }

  @Override
  public void printRequires(@Nullable final Attributable attributable) throws IOException {
    if (attributable != null) {
      final List<Requirement> requirements = new ArrayList<>();

      final boolean operator = attributable.getAttributeNotNull(PaperAttributeKeys.REQUIRES_OP);

      final ExecutorType executorType;
      if (attributable.hasAttribute(PaperAttributeKeys.EXECUTOR_TYPE)) {
        executorType = attributable.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);
      } else {
        executorType = ExecutorType.NONE;
      }

      final Requirement req = attributable.getAttribute(PaperAttributeKeys.REQUIREMENT);
      if (req != null) {
        requirements.add(req);
      }

      requirements.addAll(attributable.getAttributeNotNull(PaperAttributeKeys.PERMISSIONS)
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
}
