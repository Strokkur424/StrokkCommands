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
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.PrintParamsHolder;
import net.strokkur.commands.internal.velocity.util.SenderType;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.internal.velocity.util.VelocityCommandInformation;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

final class VelocityCommandTreePrinter extends CommonCommandTreePrinter<VelocityCommandInformation> {
  public VelocityCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final VelocityCommandInformation commandInformation,
      final ProcessingEnvironment environment,
      final PlatformUtils utils
  ) {
    super(indent, writer, node, commandInformation, environment, utils);
  }

  @Override
  protected PrintParamsHolder getParamsHolder() {
    return new PrintParamsHolder(
        SourceParameter.combineJavaDocsParameterString(
            List.of("ProxyServer"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        SourceParameter.combineMethodParameterString(
            List.of("final ProxyServer server"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        SourceParameter.combineMethodParameterNameString(
            List.of("server"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        SourceParameter.combineJavaDocsParameterString(
            List.of("ProxyServer", "Object"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        SourceParameter.combineMethodParameterString(
            List.of("final ProxyServer server", "final Object plugin"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)
        ),
        (p) -> p.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER) ? "server" : p.getName()
    );
  }

  @Override
  protected void printRegisterMethod(final PrintParamsHolder holder) throws IOException {
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
            public static void register(%s) {
                final BrigadierCommand command = new BrigadierCommand(create(%s));
                final CommandMeta meta = server.getCommandManager().metaBuilder(command)""",
        holder.createJdParams(),
        getBrigadierClassName(),
        holder.registerParams(),
        holder.createParamNames()
    );

    final Optional<String @Nullable []> aliases = Optional.ofNullable(getCommandInformation().aliases());
    if (aliases.isPresent()) {
      incrementIndent();
      incrementIndent();
      println(".aliases(%s)", String.join(", ", Arrays.stream(aliases.get())
          .map(alias -> '"' + alias + '"')
          .toList()));
      decrementIndent();
      decrementIndent();
    }

    printBlock("""        
                .plugin(plugin)
                .build();
        
            server.getCommandManager().register(meta, command);
        }""");
  }

  @Override
  public Set<String> standardImports() {
    return Set.of(
        VelocityClasses.LITERAL_ARGUMENT_BUILDER,
        VelocityClasses.BRIGADIER_COMMAND,
        VelocityClasses.COMMAND_META,
        VelocityClasses.COMMAND_SOURCE,
        VelocityClasses.PROXY_INITIALIZE_EVENT,
        VelocityClasses.PROXY_SERVER,
        Classes.NULL_MARKED
    );
  }

  @Override
  public void gatherAdditionalNodeImports(final Set<String> imports, final CommandNode node) {
    addExecutorTypeImports(imports, node.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE));
    final Executable executable = node.getEitherAttribute(AttributeKey.EXECUTABLE, AttributeKey.DEFAULT_EXECUTABLE);
    if (executable != null) {
      addExecutorTypeImports(imports, executable.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE));
    }
  }

  private void addExecutorTypeImports(final Set<String> imports, final SenderType type) {
    if (type == SenderType.NORMAL) {
      return;
    }

    if (type == SenderType.PLAYER) {
      imports.add(VelocityClasses.PLAYER);
    } else if (type == SenderType.CONSOLE) {
      imports.add(VelocityClasses.CONSOLE_COMMAND_SOURCE);
    }

    imports.add(Classes.SIMPLE_COMMAND_EXCEPTION_TYPE);
    imports.add(Classes.LITERAL_MESSAGE);
  }

  @Override
  public void prefixPrintExecutableInner(final CommandNode node, final Executable executable) throws IOException {
    final SenderType type = executable.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE);
    if (type != SenderType.NORMAL) {
      printBlock("""
              if (!(ctx.getSource() instanceof %s source)) {
                  throw new SimpleCommandExceptionType(
                      new LiteralMessage("This command requires a %s sender!")
                  ).create();
              }""",
          List.of(type.getClassName().split("\\.")).getLast(),
          type.toString().toLowerCase(Locale.ROOT)
      );
      println();
    }
  }

  @Override
  public void printFirstArguments(final Executable executable) throws IOException {
    final SenderType type = executable.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE);

    printIndent();
    if (type == SenderType.NORMAL) {
      print("ctx.getSource()");
    } else {
      print("source");
    }
  }

  @Override
  public @Nullable String getExtraRequirements(final Attributable node) {
    final Set<String> permissions = node.getAttributeNotNull(VelocityAttributeKeys.PERMISSIONS);
    final SenderType type = node.getAttributeNotNull(VelocityAttributeKeys.SENDER_TYPE);

    if (type == SenderType.NORMAL) {
      if (permissions.isEmpty()) {
        return null;
      }

      return String.join(" || ", permissions.stream()
          .map(perm -> "source.hasPermission(\"" + perm + "\")")
          .toList());
    }

    if (permissions.isEmpty()) {
      return type.getPredicate();
    }
    if (permissions.size() == 1) {
      return "%s && source.hasPermission(\"%s\")".formatted(
          type.getPredicate(),
          permissions.stream().findFirst().get()
      );
    }

    return "source -> %s && (%s))".formatted(
        type.getPredicate(),
        String.join(" || ", permissions.stream()
            .map(perm -> "source.hasPermission(\"" + perm + "\")")
            .toList())
    );
  }

  @Override
  public String getParameterName(final SourceParameter parameter) {
    if (parameter.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)) {
      return "server";
    }
    return super.getParameterName(parameter);
  }

  @Override
  public String getLiteralMethodString() {
    return "BrigadierCommand.literalArgumentBuilder";
  }

  @Override
  public String getArgumentMethodString() {
    return "BrigadierCommand.requiredArgumentBuilder";
  }
}
