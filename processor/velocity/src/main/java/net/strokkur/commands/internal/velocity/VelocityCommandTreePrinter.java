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
package net.strokkur.commands.internal.velocity;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonClassBuilder;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.printer.CommonImportPrinter;
import net.strokkur.commands.internal.printer.CommonInstanceFieldPrinter;
import net.strokkur.commands.internal.util.PrintParamsHolder;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.internal.velocity.util.VelocityCommandInformation;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

final class VelocityCommandTreePrinter extends CommonCommandTreePrinter<VelocityCommandInformation> {
  VelocityCommandTreePrinter(
      int indent,
      @Nullable Writer writer,
      CommandNode node,
      VelocityCommandInformation commandInformation,
      ProcessingEnvironment environment,
      PlatformUtils utils
  ) {
    super(indent, writer, node, commandInformation, environment, utils);
  }

  @Override
  protected void printExtraClassStart() throws IOException {
    super.collectFields();
    final Optional<String[]> aliases = Optional.ofNullable(getCommandInformation().aliases());
    if (aliases.isPresent()) {
      final String aliasesVarargs = String.join(", ", Arrays.stream(aliases.get())
          .map(alias -> '"' + alias + '"')
          .toList());
      println("public static final List<String> ALIASES = List.of(%s);", aliasesVarargs);
    }
  }

  @Override
  protected CommonImportPrinter createImportPrinter() {
    return new VelocityImportPrinter(this);
  }

  @Override
  protected CommonClassBuilder createTreePrinter() {
    return new VelocityClassBuilder(this);
  }

  @Override
  protected CommonInstanceFieldPrinter createInstanceFieldPrinter() {
    return new VelocityInstanceFieldPrinter(this);
  }

  @Override
  protected PrintParamsHolder getParamsHolder() {
    if (getCommandInformation().useInjection()) {
      return new PrintParamsHolder(
          "", "", "",
          "ProxyServer, Object", "ProxyServer server, Object command$plugin",
          SourceVariable::getName
      );
    }

    return new PrintParamsHolder(
        SourceParameter.combineJavaDocsParameterString(
            List.of("ProxyServer"),
            getCommandInformation().constructor(),
            p -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        SourceParameter.combineMethodParameterString(
            List.of("final ProxyServer server"),
            getCommandInformation().constructor(),
            p -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        SourceParameter.combineMethodParameterNameString(
            List.of("server"),
            getCommandInformation().constructor(),
            p -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        SourceParameter.combineJavaDocsParameterString(
            List.of("ProxyServer", "Object"),
            getCommandInformation().constructor(),
            p -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        SourceParameter.combineMethodParameterString(
            List.of("final ProxyServer server", "final Object command$plugin"),
            getCommandInformation().constructor(),
            p -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        p -> p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER) ? "server" : p.getName()
    );
  }

  @Override
  protected void printRegisterMethod(PrintParamsHolder holder) throws IOException {
    printBlock("""
            /**
             * Shortcut for registering the command node returned from
             * {@link #create(%s)}. This method uses the provided aliases
             * from the original source file.
             *
             * <h3>Registering the command</h3>
             * <p>
             * Commands should only be registered during the {@link ProxyInitializeEvent}.
             * The example below shows an example of how to do this. For more information,
             * refer to <a href="https://docs.papermc.io/velocity/dev/command-api/#registering-a-command">The Velocity Command API docs</a>
             *
             * <pre>{@code
             * @Subscribe
             * void onProxyInitialize(final ProxyInitializeEvent event) {
             *   %s.register(this.proxy, this);
             * }
             * }</pre>
             */
            public%s void register(%s) {
                final BrigadierCommand command = new BrigadierCommand(create(%s));
                final CommandMeta meta = server.getCommandManager().metaBuilder(command)""",
        holder.createJdParams(),
        getBrigadierClassName(),
        getCommandInformation().useInjection() ? "" : " static",
        holder.registerParams(),
        holder.createParamNames()
    );

    final Optional<?> aliases = Optional.ofNullable(getCommandInformation().aliases());
    if (aliases.isPresent()) {
      incrementIndent();
      incrementIndent();
      println(".aliases(ALIASES.toArray(String[]::new))");
      decrementIndent();
      decrementIndent();
    }

    printBlock("""
                .plugin(command$plugin)
                .build();
        
            server.getCommandManager().register(meta, command);
        }""");
  }
}
