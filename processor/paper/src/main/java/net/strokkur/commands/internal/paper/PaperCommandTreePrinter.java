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
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.PaperCommandInformation;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.printer.CommonImportPrinter;
import net.strokkur.commands.internal.printer.CommonTreePrinter;
import net.strokkur.commands.internal.util.PrintParamsHolder;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

final class PaperCommandTreePrinter extends CommonCommandTreePrinter<PaperCommandInformation> {

  PaperCommandTreePrinter(
      int indent,
      @Nullable Writer writer,
      CommandNode node,
      PaperCommandInformation commandInformation,
      ProcessingEnvironment environment,
      PlatformUtils platformUtils
  ) {
    super(indent, writer, node, commandInformation, environment, platformUtils);
  }

  @Override
  protected CommonImportPrinter createImportPrinter() {
    return new PaperImportPrinter(this);
  }

  @Override
  protected CommonTreePrinter createTreePrinter() {
    return new PaperTreePrinter(this);
  }

  @Override
  protected PrintParamsHolder getParamsHolder() {
    if (getCommandInformation().useInjection()) {
      return new PrintParamsHolder(
          "", "", "",
          "Commands", "Commands commands",
          SourceVariable::getName
      );
    }
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
  protected void printExtraClassStart() throws IOException {
    super.printExtraClassStart();
    final String description = getCommandInformation().description() == null ? "null" : '"' + getCommandInformation().description() + '"';
    final String aliases = getCommandInformation().aliases() == null ? "" : '"' + String.join("\", \"", List.of(getCommandInformation().aliases())) + '"';
    println("public static final @Nullable String DESCRIPTION = %s;", description);
    println("public static final List<String> ALIASES = List.of(%s);", aliases);
  }

  @Override
  protected void printRegisterMethod(PrintParamsHolder holder) throws IOException {
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
            public%s%s void register(%s) {
                commands.register(create(%s), DESCRIPTION, ALIASES);
            }""",
        holder.createJdParams(),
        getBrigadierClassName(),
        getCommandInformation().useInjection() ? "" : " static",
        Optional.ofNullable(getCommandInformation().constructor())
            .map(SourceMethod::getCombinedTypeAnnotationsString)
            .orElse(""),
        holder.registerParams(),
        holder.createParamNames()
    );
  }

  @Override
  protected void printSemicolon() throws IOException {
    println();
    printIndented(".build();");
  }
}
