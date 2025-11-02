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
package net.strokkur.commands.internal.modded;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.modded.util.ModdedClasses;
import net.strokkur.commands.internal.modded.util.ModdedCommandInformation;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.PrintParamsHolder;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class ModdedCommandTreePrinter extends CommonCommandTreePrinter<ModdedCommandInformation> {
  public ModdedCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final ModdedCommandInformation commandInformation,
      final ProcessingEnvironment environment,
      final PlatformUtils utils
  ) {
    super(indent, writer, node, commandInformation, environment, utils);
  }

  protected String getSourceName() {
    return "CommandSourceStack";
  }

  protected abstract void printerRegisterJavaDoc() throws IOException;

  @Override
  protected final PrintParamsHolder getParamsHolder() {
    return new PrintParamsHolder(
        SourceParameter.combineJavaDocsParameterString(
            List.of("String", "CommandBuildContext"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(ModdedClasses.COMMAND_BUILD_CONTEXT)
        ),
        SourceParameter.combineMethodParameterString(
            List.of("final String commandName", "final CommandBuildContext registryAccess"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(ModdedClasses.COMMAND_BUILD_CONTEXT)
        ),
        SourceParameter.combineMethodParameterString(
            List.of("alias", "registryAccess"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(ModdedClasses.COMMAND_BUILD_CONTEXT)
        ),
        SourceParameter.combineJavaDocsParameterString(
            List.of("CommandDispatcher", "CommandBuildContext"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(ModdedClasses.COMMAND_BUILD_CONTEXT)
        ),
        SourceParameter.combineMethodParameterNameString(
            List.of("final CommandDispatcher<%s> dispatcher".formatted(getSourceName()), "final CommandBuildContext registryAccess"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(ModdedClasses.COMMAND_BUILD_CONTEXT)
        ),
        (p) -> p.getType().getFullyQualifiedName().equals(ModdedClasses.COMMAND_BUILD_CONTEXT) ? "registryAccess" : p.getName()
    );
  }

  @Override
  protected final void printRegisterMethod(final PrintParamsHolder holder) throws IOException {
    final List<String> namesToRegister = new ArrayList<>();
    namesToRegister.add(((LiteralCommandArgument) node.argument()).literal());
    namesToRegister.addAll(List.of(getCommandInformation().aliases()));

    final String aliases = '"' + String.join("\", \"", namesToRegister) + '"';
    printBlock("""
            /**
             * Shortcut for registering the command node(s) returned from
             * {@link #create(%s)}. This method uses the provided aliases
             * from the original source file.
             *
             * <h3>Registering the command</h3>
             *""",
        holder.createJdParams()
    );

    printerRegisterJavaDoc();

    printBlock("""
             */
            public static%s void register(%s) {
                for (final String alias : List.of(%s)) {
                    dispatcher.register(create(%s));
                }
            }""",
        Optional.ofNullable(getCommandInformation().constructor())
            .map(SourceMethod::getCombinedTypeAnnotationsString)
            .orElse(""),
        holder.registerParams(),
        aliases,
        holder.createParamNames()
    );
  }

  @Override
  public Set<String> standardImports() {
    return Set.of(
        ModdedClasses.COMMAND,
        ModdedClasses.COMMAND_DISPATCHER,
        ModdedClasses.COMMAND_BUILD_CONTEXT,
        ModdedClasses.LITERAL_ARGUMENT_BUILDER,
        ModdedClasses.LIST,
        ModdedClasses.NULL_MARKED
    );
  }

  @Override
  public final String getCommandNameLiteralOverride(final LiteralCommandArgument lit) {
    return "commandName";
  }

  @Override
  public final void prefixPrintExecutableInner(final CommandNode node, final Executable executable) throws IOException {
    // noop
  }

  @Override
  public final void printFirstArguments(final Executable executable) throws IOException {
    printIndented("ctx.getSource()");
  }

  @Override
  public final @Nullable String getExtraRequirements(final Attributable node) {
    return null;
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
