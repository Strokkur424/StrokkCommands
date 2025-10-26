package net.strokkur.commands.internal.fabric.client.mojang;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.SourceMethod;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.fabric.client.mojang.util.FabricClasses;
import net.strokkur.commands.internal.fabric.client.mojang.util.FabricCommandInformation;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.Executable;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
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

final class FabricCommandTreePrinter extends CommonCommandTreePrinter<FabricCommandInformation> {
  public FabricCommandTreePrinter(
      final int indent,
      final @Nullable Writer writer,
      final CommandNode node,
      final FabricCommandInformation commandInformation,
      final ProcessingEnvironment environment,
      final PlatformUtils utils
  ) {
    super(indent, writer, node, commandInformation, environment, utils);
  }

  @Override
  protected PrintParamsHolder getParamsHolder() {
    return new PrintParamsHolder(
        SourceParameter.combineJavaDocsParameterString(
            List.of("String", "CommandBuildContext"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(FabricClasses.COMMAND_BUILD_CONTEXT)
        ),
        SourceParameter.combineMethodParameterString(
            List.of("final String commandName", "final CommandBuildContext registryAccess"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(FabricClasses.COMMAND_BUILD_CONTEXT)
        ),
        SourceParameter.combineMethodParameterString(
            List.of("alias", "registryAccess"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(FabricClasses.COMMAND_BUILD_CONTEXT)
        ),
        SourceParameter.combineJavaDocsParameterString(
            List.of("CommandDispatcher", "CommandBuildContext"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(FabricClasses.COMMAND_BUILD_CONTEXT)
        ),
        SourceParameter.combineMethodParameterNameString(
            List.of("final CommandDispatcher<FabricClientCommandSource> dispatcher", "final CommandBuildContext registryAccess"),
            getCommandInformation().constructor(),
            (p) -> !p.getType().getFullyQualifiedName().equals(FabricClasses.COMMAND_BUILD_CONTEXT)
        ),
        (p) -> p.getType().getFullyQualifiedName().equals(FabricClasses.COMMAND_BUILD_CONTEXT) ? "registryAccess" : p.getName()
    );
  }

  @Override
  protected void printRegisterMethod(final PrintParamsHolder holder) throws IOException {
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
             *
             * This method should be called in your client main class' {@link ClientModInitializer#onInitializeClient()} method
             * inside of a {@link ClientCommandRegistrationCallback} event. You can find some information on commands
             * in the <a href="https://docs.fabricmc.net/develop/commands/basics">Fabric Documentation</a>.
             * <p>
             * <pre>{@code
             * ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
             *   %s.register(dispatcher, registryAccess);
             * });
             * }</pre>
             */
            public static%s void register(%s) {
                for (final String alias : List.of(%s)) {
                    dispatcher.register(create(%s));
                }
            }""",
        holder.createJdParams(),
        getBrigadierClassName(),
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
        FabricClasses.COMMAND,
        FabricClasses.COMMAND_DISPATCHER,
        FabricClasses.COMMAND_BUILD_CONTEXT,
        FabricClasses.CLIENT_COMMAND_MANAGER,
        FabricClasses.CLIENT_COMMAND_REGISTRATION_CALLBACK,
        FabricClasses.CLIENT_MOD_INITIALIZER,
        FabricClasses.FABRIC_CLIENT_COMMAND_SOURCE,
        FabricClasses.LITERAL_ARGUMENT_BUILDER,
        FabricClasses.LIST,
        FabricClasses.NULL_MARKED
    );
  }

  @Override
  public String getCommandNameLiteralOverride(final LiteralCommandArgument lit) {
    return "commandName";
  }

  @Override
  public void prefixPrintExecutableInner(final CommandNode node, final Executable executable) throws IOException {
    // noop
  }

  @Override
  public void printFirstArguments(final Executable executable) throws IOException {
    printIndented("ctx.getSource()");
  }

  @Override
  public @Nullable String getExtraRequirements(final Attributable node) {
    return null;
  }

  @Override
  public String getLiteralMethodString() {
    return "ClientCommandManager.literal";
  }

  @Override
  public String getArgumentMethodString() {
    return "ClientCommandManager.argument";
  }
}
