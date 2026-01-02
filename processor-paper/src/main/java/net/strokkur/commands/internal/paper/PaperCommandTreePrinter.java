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
package net.strokkur.commands.internal.paper;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.ExecutorType;
import net.strokkur.commands.internal.paper.util.PaperAttributeKeys;
import net.strokkur.commands.internal.paper.util.PaperClasses;
import net.strokkur.commands.internal.paper.util.PaperCommandInformation;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.PrintParamsHolder;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class PaperCommandTreePrinter extends CommonCommandTreePrinter<PaperCommandInformation> {

  public PaperCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final PaperCommandInformation commandInformation,
      final ProcessingEnvironment environment,
      final PlatformUtils platformUtils
  ) {
    super(indent, writer, node, commandInformation, environment, platformUtils);
  }

  @Override
  protected PrintParamsHolder getParamsHolder() {
    return new PrintParamsHolder(
        SourceParameter.combineJavaDocsParameterString(List.of(), getCommandInformation().constructor(), (p) -> true),
        SourceParameter.combineMethodParameterString(List.of(), getCommandInformation().constructor(), (p) -> true),
        SourceParameter.combineMethodParameterNameString(List.of(), getCommandInformation().constructor(), (p) -> true),
        SourceParameter.combineJavaDocsParameterString(List.of("Commands"), getCommandInformation().constructor(), (p) -> true),
        SourceParameter.combineMethodParameterString(List.of("final Commands commands"), getCommandInformation().constructor(), (p) -> true),
        SourceVariable::getName
    );
  }

  @Override
  protected void printRegisterMethod(final PrintParamsHolder holder) throws IOException {
    final String description = getCommandInformation().description() == null ? "null" : '"' + getCommandInformation().description() + '"';
    final String aliases = getCommandInformation().aliases() == null ? "" : '"' + String.join("\", \"", List.of(getCommandInformation().aliases())) + '"';

    printBlock("""
            /**
             * Shortcut for registering the command node returned from
             * {@link #create(%s)}. This method uses the provided aliases
             * and description from the original source file.
             * <p>
             * <h3>Registering the command</h3>
             * <p>
             * This method can safely be called either in your plugin bootstrapper's
             * {@link PluginBootstrap#bootstrap(BootstrapContext)}, your main
             * class' {@link JavaPlugin#onLoad()} or {@link JavaPlugin#onEnable()}
             * method.
             * <p>
             * You need to call it inside of a lifecycle event. General information can be found on the
             * <a href="https://docs.papermc.io/paper/dev/lifecycle/">PaperMC Lifecycle API docs page</a>.
             * The general use case might look like this (example given inside the {@code onEnable} method):
             * <p>
             * <pre>{@code
             * this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
             *     final Commands commands = event.registrar();
             *     %s.register(commands);
             * }
             * }</pre>
             */
            public static%s void register(%s) {
                commands.register(create(%s), %s, List.of(%s));
            }""",
        holder.createJdParams(),
        getBrigadierClassName(),
        Optional.ofNullable(getCommandInformation().constructor())
            .map(SourceMethod::getCombinedTypeAnnotationsString)
            .orElse(""),
        holder.registerParams(),
        holder.createParamNames(),
        description,
        aliases
    );
  }

  @Override
  protected void printSemicolon() throws IOException {
    println();
    printIndented(".build();");
  }

  @Override
  public Set<String> standardImports() {
    return Set.of(
        Classes.COMMAND,
        Classes.LITERAL_COMMAND_NODE,
        PaperClasses.COMMAND_SOURCE_STACK,
        PaperClasses.COMMANDS,
        Classes.LIST,
        Classes.NULL_MARKED,
        "io.papermc.paper.plugin.bootstrap.PluginBootstrap",
        "io.papermc.paper.plugin.bootstrap.BootstrapContext",
        "org.bukkit.plugin.java.JavaPlugin"
    );
  }

  @Override
  public void gatherAdditionalNodeImports(final Set<String> imports, final CommandNode node) {
    addExecutorTypeImports(imports, node.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE));
    final Executable executable = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      addExecutorTypeImports(imports, executable.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE));
    }
  }

  @Override
  public void gatherAdditionalExecutorWrapperImports(final Set<String> imports) {
    imports.add(PaperClasses.COMMAND_SENDER);
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

    print(",");
    println();
    printIndent();
    print("executor");
  }

  @Override
  public @Nullable String getExtraRequirements(final Attributable node) {
    final List<String> extraRequirements = new ArrayList<>();

    final ExecutorType executorType = node.getAttributeNotNull(PaperAttributeKeys.EXECUTOR_TYPE);
    if (executorType != ExecutorType.NONE) {
      extraRequirements.add(executorType.getPredicate());
    }

    final boolean operator = node.getAttributeNotNull(PaperAttributeKeys.REQUIRES_OP);
    if (operator) {
      extraRequirements.add("source.getSender().isOp()");
    }

    final List<String> permissions = node.getAttributeNotNull(PaperAttributeKeys.PERMISSIONS).stream()
        .map("source.getSender().hasPermission(\"%s\")"::formatted)
        .toList();

    if (!permissions.isEmpty()) {
      if (permissions.size() == 1) {
        extraRequirements.add(permissions.getFirst());
      } else {
        extraRequirements.add('(' + String.join(" || ", permissions) + ')');
      }
    }

    return extraRequirements.isEmpty() ? null : String.join(" && ", extraRequirements);
  }

  @Override
  public String getLiteralMethodString() {
    return "Commands.literal";
  }

  @Override
  public String getArgumentMethodString() {
    return "Commands.argument";
  }
}
