package net.strokkur.commands.internal.codegen.integration;

import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.InvokesMethod;
import net.strokkur.commands.internal.codegen.Modifiers;
import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.codegen.builder.MethodBuilder;
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import net.strokkur.commands.internal.printer.javadoc.JavaStarJavadocVisitor;
import net.strokkur.commands.internal.printer.source.AbstractSourcePrintingVisitor;
import net.strokkur.commands.internal.printer.source.ImportGatheringVisitor;
import net.strokkur.commands.internal.printer.source.JavaSourcePrintingVisitor;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FullVelocityIntegrationTest {
  private static final CodeType.ClassType COMMAND = CodeType.ofClass("com.mojang.brigadier.Command");
  private static final CodeType.ClassType LITERAL_MESSAGE = CodeType.ofClass("com.mojang.brigadier.LiteralMessage");
  private static final CodeType.ClassType STRING_ARGUMENT_TYPE = CodeType.ofClass("com.mojang.brigadier.arguments.StringArgumentType");
  private static final CodeType.ClassType COMMAND_SOURCE = CodeType.ofClass("com.velocitypowered.api.command.CommandSource");
  private static final CodeType.ClassType LITERAL_ARGUMENT_BUILDER = CodeType.ofClassTyped(
      "com.mojang.brigadier.builder.LiteralArgumentBuilder", COMMAND_SOURCE
  );
  private static final CodeType.ClassType SIMPLE_COMMAND_EXCEPTION_TYPE = CodeType.ofClass("com.mojang.brigadier.exceptions.SimpleCommandExceptionType");
  private static final CodeType.ClassType BRIGADIER_COMMAND = CodeType.ofClass("com.velocitypowered.api.command.BrigadierCommand");
  private static final CodeType.ClassType COMMAND_META = CodeType.ofClass("com.velocitypowered.api.command.CommandMeta");
  private static final CodeType.ClassType PLAYER = CodeType.ofClass("com.velocitypowered.api.proxy.Player");
  private static final CodeType.ClassType PROXY_SERVER = CodeType.ofClass("com.velocitypowered.api.proxy.ProxyServer");
  private static final CodeType.ClassType INJECT = CodeType.ofClass("jakarta.inject.Inject");
  private static final CodeType.ClassType NULL_MARKED = CodeType.ofClass("org.jspecify.annotations.NullMarked");

  private static final CodeType.ClassType TEST_COMMAND = CodeType.ofClass("net.strokkur.testplugin.velocity.reference.TestCommand");
  private static final CodeType.ClassType PROXY_INITIALIZE_EVENT = CodeType.ofClass("com.velocitypowered.api.event.proxy.ProxyInitializeEvent");

  //<editor-fold desc="expected">
  private static final Set<CodeType.ClassType> expectedImportedClasses = Set.of(
      COMMAND, LITERAL_MESSAGE, STRING_ARGUMENT_TYPE, LITERAL_ARGUMENT_BUILDER,
      SIMPLE_COMMAND_EXCEPTION_TYPE, BRIGADIER_COMMAND, COMMAND_META,
      COMMAND_SOURCE, PLAYER, PROXY_SERVER, INJECT, NULL_MARKED, CodeType.LIST
  );

  @Language("JAVA")
  private static final String expectedCode = """
      /**
       * A class holding the Brigadier source tree generated from
       * {@link TestCommand} using <a href="https://commands.strokkur.net">StrokkCommands</a>.
       *
       * @author Strokkur24 - StrokkCommands
       * @version 2.1.3
       * @see #create() creating the LiteralArgumentBuilder
       * @see #register(ProxyServer, Object) registering the command
       */
      @NullMarked
      public final class TestCommandBrigadier {
        public static final String NAME = "testcommand";
        public static final List<String> ALIASES = List.of("test");
      
        private @Inject TestCommand instance;
      
        /**
         * Shortcut for registering the command node returned from
         * {@link #create()}. This method uses the provided aliases
         * from the original source file.
         *
         * <h3>Registering the command</h3>
         *
         * Commands should only be registered during the {@link com.velocitypowered.api.event.proxy.ProxyInitializeEvent}.
         * The example below shows an example of how to do this. For more information,
         * refer to <a href="https://docs.papermc.io/velocity/dev/command-api/#registering-a-command">The Velocity Command API docs</a>
         *
         * <pre>{@code
         * @Subscribe
         * void onProxyInitialize(final ProxyInitializeEvent event) {
         *   TestCommandBrigadier.register(this.proxy, this);
         * }
         * }</pre>
         */
        public void register(ProxyServer server, Object command$plugin) {
          final BrigadierCommand command = new BrigadierCommand(create());
          final CommandMeta meta = server.getCommandManager().metaBuilder(command)
              .aliases(ALIASES.toArray(String[]::new))
              .plugin(command$plugin)
              .build();
      
          server.getCommandManager().register(meta, command);
        }
      
        /**
         * A method for creating a Brigadier command node which denotes the declared command
         * in {@link TestCommand}. You can either retrieve the unregistered node with this method
         * or register it directly with {@link #register(ProxyServer, Object)}.
         */
        public LiteralArgumentBuilder<CommandSource> create() {
          return BrigadierCommand.literalArgumentBuilder(NAME)
              .requires(source -> source.hasPermission("testcommand.use"))
              .executes(ctx -> {
                instance.execute(ctx.getSource());
                return Command.SINGLE_SUCCESS;
              })
              .then(BrigadierCommand.literalArgumentBuilder("run")
                  .executes(ctx -> {
                    if (!(ctx.getSource() instanceof Player source)) {
                      throw new SimpleCommandExceptionType(
                          new LiteralMessage("This command requires a player sender!")
                      ).create();
                    }
      
                    instance.run(source);
                    return Command.SINGLE_SUCCESS;
                  })
                  .then(BrigadierCommand.requiredArgumentBuilder("target", StringArgumentType.word())
                      .executes(ctx -> {
                        instance.runWithTarget(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "target")
                        );
                        return Command.SINGLE_SUCCESS;
                      })
                  )
              );
        }
      }
      """;
  //</editor-fold>

  @Test
  void testFullExample() {
    final CodeClass builtClass = buildClass();

    final ImportGatheringVisitor importVisitor = new ImportGatheringVisitor();
    final Set<CodeType.ClassType> imports = builtClass.accept(importVisitor).stream()
        .filter(gathered -> !CodePackage.isRedundantImport(builtClass.codePackage(), gathered.codePackage()))
        .collect(Collectors.toSet());

    // Check if imports match
    final List<String> sortedImports = imports.stream().map(CodeType::fullyQualifiedName).sorted().toList();
    final List<String> sortedExpectedImports = expectedImportedClasses.stream()
        .map(CodeType::fullyQualifiedName).sorted().toList();

    assertEquals(sortedExpectedImports.size(), sortedImports.size());
    for (int i = 0, sortedImportsSize = sortedImports.size(); i < sortedImportsSize; i++) {
      final String expectedImport = sortedExpectedImports.get(i);
      final String actualImport = sortedImports.get(i);
      assertEquals(expectedImport, actualImport);
    }

    // Check generated class file
    final AbstractSourcePrintingVisitor sourceVisitor = new JavaSourcePrintingVisitor(
        () -> new JavaStarJavadocVisitor(builtClass.codePackage(), imports), "  ", "    ");

    final StringBuilder result = builtClass.accept(sourceVisitor);
    assertEquals(expectedCode, result.toString());
  }

  private CodeClass buildClass() {
    final MethodBuilder registerMethod = Builders.method("register")
        .addParameter(PROXY_SERVER, "server")
        .addParameter(CodeType.OBJECT, "command$plugin");
    final MethodBuilder createMethod = Builders.method("create");

    return Builders.classBuilder(TEST_COMMAND.fullyQualifiedName() + "Brigadier")
        .setJavadoc(CodeJavadoc.combineLines(
            CodeJavadoc.text("A class holding the Brigadier source tree generated from"),
            CodeJavadoc.combine(
                CodeJavadoc.classReference(TEST_COMMAND.codeClass()),
                CodeJavadoc.text(" using "),
                CodeJavadoc.url("StrokkCommands", "https://commands.strokkur.net"),
                CodeJavadoc.text(".")
            ),
            CodeJavadoc.blank(),
            CodeJavadoc.author("Strokkur24 - StrokkCommands"),
            CodeJavadoc.version("2.1.3"),
            CodeJavadoc.see(createMethod, "creating the LiteralArgumentBuilder", true),
            CodeJavadoc.see(registerMethod, "registering the command", true)))
        .addAnnotations(CodeAnnotation.of(NULL_MARKED))
        .setModifiers(Modifiers.FINAL, Modifiers.PUBLIC)

        // Static fields
        .addField(Builders.field("NAME", CodeType.STRING)
            .setModifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
            .setInitialiser(CodeExpression.string("testcommand"))
        )
        .addField(Builders.field("ALIASES", CodeType.LIST_STRING)
            .setModifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
            .setInitialiser(Builders.methodInvocation("of")
                .addParameter(CodeExpression.string("test"))
                .setStatic(CodeType.LIST)
            )
        )

        // Instance fields
        .addField(Builders.field("instance", TEST_COMMAND)
            .addAnnotation(CodeAnnotation.of(INJECT))
            .setModifiers(Modifiers.PRIVATE)
        )

        // Register method
        .addMethod(registerMethod
            .setJavadoc(CodeJavadoc.combineLines(
                CodeJavadoc.text("Shortcut for registering the command node returned from"),
                CodeJavadoc.combine(CodeJavadoc.methodReference(createMethod, true), CodeJavadoc.text(". This method uses the provided aliases")),
                CodeJavadoc.text("from the original source file."),

                CodeJavadoc.header("Registering the command", 3),

                CodeJavadoc.combine(
                    CodeJavadoc.text("Commands should only be registered during the "),
                    CodeJavadoc.classReference(PROXY_INITIALIZE_EVENT.codeClass()),
                    CodeJavadoc.text(".")
                ),
                CodeJavadoc.text("The example below shows an example of how to do this. For more information,"),
                CodeJavadoc.combine(CodeJavadoc.text("refer to "), CodeJavadoc.url("The Velocity Command API docs", "https://docs.papermc.io/velocity/dev/command-api/#registering-a-command")),

                CodeJavadoc.blank(),

                CodeJavadoc.codeBlock("""
                    @Subscribe
                    void onProxyInitialize(final ProxyInitializeEvent event) {
                      TestCommandBrigadier.register(this.proxy, this);
                    }""")
            ))
            .setModifiers(Modifiers.PUBLIC)

            .setCodeBlock(
                CodeStatement.variableDeclarationFinal(BRIGADIER_COMMAND, "command", Builders.ctorInvocation(BRIGADIER_COMMAND)
                    .addParameter(Builders.methodInvocation("create"))),
                CodeStatement.variableDeclarationFinal(COMMAND_META, "meta", Builders.methodInvocation("getCommandManager")
                    .setInstanceVariable("server")
                    .chain("metaBuilder", CodeExpression.variable("command"))
                    .chain("aliases", InvokesMethod.StyleConfig.NEWLINE, Builders.methodInvocation("toArray")
                        .setInstanceVariable("ALIASES")
                        .addParameter(CodeExpression.methodReference(CodeType.STRING_ARRAY, "new")))
                    .chain("plugin", InvokesMethod.StyleConfig.NEWLINE, CodeExpression.variable("command$plugin"))
                    .chain("build", InvokesMethod.StyleConfig.NEWLINE)),
                CodeStatement.blank(),
                Builders.methodInvocation("getCommandManager")
                    .setInstanceVariable("server")
                    .chain("register", CodeExpression.variable("meta"), CodeExpression.variable("command"))
            )
        )

        // Create method
        .addMethod(createMethod
            .setModifiers(Modifiers.PUBLIC)
            .setReturnType(LITERAL_ARGUMENT_BUILDER)
            .setJavadoc(CodeJavadoc.combineLines(
                CodeJavadoc.text("A method for creating a Brigadier command node which denotes the declared command"),
                CodeJavadoc.combine(
                    CodeJavadoc.text("in "),
                    CodeJavadoc.classReference(TEST_COMMAND.codeClass()),
                    CodeJavadoc.text(". You can either retrieve the unregistered node with this method")),
                CodeJavadoc.combine(
                    CodeJavadoc.text("or register it directly with "),
                    CodeJavadoc.methodReference(registerMethod, true),
                    CodeJavadoc.text(".")
                )
            ))

            .setCodeBlock(CodeStatement.returnStatement(
                Builders.methodInvocation("literalArgumentBuilder")
                    .setStatic(BRIGADIER_COMMAND)
                    .addParameter(CodeExpression.variable("NAME"))

                    .chain("requires", InvokesMethod.StyleConfig.NEWLINE, CodeExpression.lambda(
                        List.of("source"),
                        Builders.methodInvocation("hasPermission")
                            .setInstanceVariable("source")
                            .addParameter(CodeExpression.string("testcommand.use"))
                    ))

                    .chain("executes", InvokesMethod.StyleConfig.NEWLINE, CodeExpression.lambda(
                        List.of("ctx"),
                        Builders.methodInvocation("execute")
                            .setInstanceVariable("instance")
                            .addParameter(Builders.methodInvocation("getSource").setInstanceVariable("ctx")),
                        CodeStatement.returnStatement(Builders.fieldAccess("SINGLE_SUCCESS")
                            .setStatic(COMMAND)
                        )
                    ))

                    .chain("then", InvokesMethod.StyleConfig.NEWLINE_BOTH, Builders.methodInvocation("literalArgumentBuilder")
                        .setStatic(BRIGADIER_COMMAND)
                        .addParameter(CodeExpression.string("run"))

                        .chain("executes", InvokesMethod.StyleConfig.NEWLINE, CodeExpression.lambda(
                            List.of("ctx"),
                            CodeStatement.ifStmt(
                                CodeExpression.instanceofExpr(
                                    Builders.methodInvocation("getSource").setInstanceVariable("ctx"),
                                    PLAYER,
                                    "source"
                                ).invert(),
                                CodeStatement.throwStatement(Builders.ctorInvocation(SIMPLE_COMMAND_EXCEPTION_TYPE)
                                    .setMultilineParameters()
                                    .addParameter(Builders.ctorInvocation(LITERAL_MESSAGE)
                                        .addParameter(CodeExpression.string("This command requires a player sender!")))
                                    .chain("create")
                                )
                            ),
                            CodeStatement.blank(),
                            Builders.methodInvocation("run")
                                .setInstanceVariable("instance")
                                .addParameter(CodeExpression.variable("source")),
                            CodeStatement.returnStatement(Builders.fieldAccess("SINGLE_SUCCESS")
                                .setStatic(COMMAND)
                            )
                        ))

                        .chain("then", InvokesMethod.StyleConfig.NEWLINE_BOTH, Builders.methodInvocation("requiredArgumentBuilder")
                            .setStatic(BRIGADIER_COMMAND)
                            .addParameter(CodeExpression.string("target"))
                            .addParameter(Builders.methodInvocation("word").setStatic(STRING_ARGUMENT_TYPE))
                            .chain("executes", InvokesMethod.StyleConfig.NEWLINE, CodeExpression.lambda(
                                List.of("ctx"),
                                Builders.methodInvocation("runWithTarget")
                                    .setMultilineParameters()
                                    .setInstanceVariable("instance")
                                    .addParameter(Builders.methodInvocation("getSource").setInstanceVariable("ctx"))
                                    .addParameter(Builders.methodInvocation("getString")
                                        .setStatic(STRING_ARGUMENT_TYPE)
                                        .addParameter(CodeExpression.variable("ctx"))
                                        .addParameter(CodeExpression.string("target"))
                                    ),
                                CodeStatement.returnStatement(Builders.fieldAccess("SINGLE_SUCCESS")
                                    .setStatic(COMMAND)
                                )
                            ))
                        )
                    )
            ))
        )
        .build();
  }

}
