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
package net.strokkur.commands.internal.codegen;

import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.codegen.visitor.CodeVisitable;
import net.strokkur.commands.internal.printer.javadoc.JavaMarkdownJavadocVisitor;
import net.strokkur.commands.internal.printer.source.JavaSourcePrintingVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaCodeGenTests {
  private static final CodeClass EXAMPLE_CLASS = Builders.classBuilder("com.example.ExampleClass").build();

  private void check(String expected, CodeVisitable visitable) {
    final JavaSourcePrintingVisitor visitor = new JavaSourcePrintingVisitor(JavaMarkdownJavadocVisitor::new, "  ", "  ");
    final String actual = visitable.accept(visitor).toString();
    assertEquals(expected, actual);
  }

  @Test
  void testPackageThrows() {
    assertThrows(IllegalStateException.class, () -> {
      CodePackage.of("com.example").accept(new JavaSourcePrintingVisitor(JavaMarkdownJavadocVisitor::new, "  ", "  "));
    });
  }

  @Test
  void testClass() {
    // language=java
    final String expected = """
        class ExampleClass {
        }
        """;
    check(expected, EXAMPLE_CLASS);

    // language=java
    final String expectedWithFields = """
        class ExampleClass {
          String oneField;
          int anotherField;
        }
        """;
    check(expectedWithFields, Builders.classBuilder(EXAMPLE_CLASS.fullyQualifiedName())
        .addField(Builders.field("oneField", CodeType.ofClass(CodeClass.STRING)))
        .addField(Builders.field("anotherField", CodeType.INT))
        .build()
    );

    // language=java
    final String expectedWithMethods = """
        class ExampleClass {
        
          String oneMethod() {
          }
        
          int anotherMethod() {
          }
        }
        """;
    check(expectedWithMethods, Builders.classBuilder(EXAMPLE_CLASS.fullyQualifiedName())
        .addMethod(Builders.method(EXAMPLE_CLASS, "oneMethod")
            .setReturnType(CodeType.ofClass(CodeClass.STRING))
        )
        .addMethod(Builders.method(EXAMPLE_CLASS, "anotherMethod")
            .setReturnType(CodeType.INT)
        )
        .build()
    );

    // language=java
    final String expectedCombined = """
        class ExampleClass {
          String oneField;
          int anotherField;
        
          static String oneStaticMethod() {
          }
        
          static int anotherStaticMethod() {
          }
        
          ExampleClass() {
          }
        
          String oneInstanceMethod() {
          }
        
          int anotherInstanceMethod() {
          }
        }
        """;
    check(expectedCombined, Builders.classBuilder(EXAMPLE_CLASS.fullyQualifiedName())
        .addField(Builders.field("oneField", CodeType.ofClass(CodeClass.STRING)))
        .addField(Builders.field("anotherField", CodeType.INT))
        .addMethod(Builders.method(EXAMPLE_CLASS, "oneStaticMethod")
            .setModifiers(Set.of(Modifiers.STATIC))
            .setReturnType(CodeType.ofClass(CodeClass.STRING))
        )
        .addMethod(Builders.method(EXAMPLE_CLASS, "anotherStaticMethod")
            .setModifiers(Set.of(Modifiers.STATIC))
            .setReturnType(CodeType.INT)
        )
        .addMethod(Builders.method(EXAMPLE_CLASS, "oneInstanceMethod")
            .setReturnType(CodeType.ofClass(CodeClass.STRING))
        )
        .addMethod(Builders.method(EXAMPLE_CLASS, "anotherInstanceMethod")
            .setReturnType(CodeType.INT)
        )
        .addMethod(Builders.method(EXAMPLE_CLASS)
            .buildConstructor()
        )
        .build()
    );
  }

  @Test
  void testMethod() {
    // language=java
    final String expected = """
        void method() {
        }
        """;
    check(expected, Builders.method(EXAMPLE_CLASS, "method").build());

    // language=java
    final String expectedWithModifiers = """
        public static void method() {
        }
        """;
    check(expectedWithModifiers, Builders.method(EXAMPLE_CLASS, "method")
        .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.STATIC))
        .build());

    // language=java
    final String expectedWithStatements = """
        void method() {
          otherMethod(STATIC_VALUE, "Now");
        }
        """;
    check(expectedWithStatements, Builders.method(EXAMPLE_CLASS, "method")
        .setMethodStatements(List.of(
            Builders.methodInvocation(Builders.method(EXAMPLE_CLASS, "otherMethod"))
                .addParameter(CodeExpression.variable("STATIC_VALUE"))
                .addParameter(CodeExpression.string("Now"))
        )).build()
    );

    // language=java
    final String expectedWithThrows = """
        void method() throws NullPointerException, SQLException {
          throw new SQLException("No database present :(");
        }
        """;
    check(expectedWithThrows, Builders.method(EXAMPLE_CLASS, "method")
        .setThrowsExceptions(
            CodeType.ofClass("java.lang.NullPointerException"),
            CodeType.ofClass("java.sql.SQLException")
        )
        .setMethodStatements(List.of(
            CodeStatement.throwStatement(Builders.ctorInvocation(CodeType.ofClass("java.sql.SQLException"))
                .addParameter(CodeExpression.string("No database present :("))
            )
        ))
        .build());
  }

  @Test
  void testField() {
    // language=java
    final String expectedNoInit = """
        String someField;
        """;
    check(expectedNoInit, Builders.field("someField", CodeType.ofClass(CodeClass.STRING)).build());

    // language=java
    final String expectedWithInit = """
        String someField = "some value";
        """;
    check(expectedWithInit, Builders.field("someField", CodeType.ofClass(CodeClass.STRING))
        .setInitialiser(CodeExpression.string("some value"))
        .build());

    // language=java
    final String expectedWithModifiers = """
        public static final String SOME_FIELD;
        """;
    check(expectedWithModifiers, Builders.field("SOME_FIELD", CodeType.ofClass(CodeClass.STRING))
        .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL))
        .build());
  }

  @Test
  void testStatement() {
    final @JavaStatements String expectedReturnStmtEmpty = """
        return;
        """;
    check(expectedReturnStmtEmpty, CodeStatement.returnStatement(null));

    final @JavaStatements String expectedReturnStmtWithExpr = """
        return value;
        """;
    check(expectedReturnStmtWithExpr, CodeStatement.returnStatement(
        CodeExpression.variable("value")
    ));

    final @JavaStatements String expectedVariableDeclaration = """
        String value = "burger";
        """;
    check(expectedVariableDeclaration, CodeStatement.variableDeclaration(
        CodeType.ofClass(CodeClass.STRING),
        "value",
        CodeExpression.string("burger")
    ));

    final @JavaStatements String expectedVariableDeclarationNoInit = """
        String value;
        """;
    check(expectedVariableDeclarationNoInit, CodeStatement.variableDeclaration(
        CodeType.ofClass(CodeClass.STRING),
        "value",
        null
    ));
  }

  @Test
  void methodInvocationFormat() {
    final @JavaStatements String multiline = """
        String
          .getValue()
          .stream().toLol()
          .toList();
        """;
    check(multiline, Builders.methodInvocation("getValue")
        .setStatic()
        .setType(CodeType.STRING)
        .setNewline()
        .chain("stream", InvokesMethod.StyleConfig.NEWLINE)
        .chain("toLol")
        .chain("toList", InvokesMethod.StyleConfig.NEWLINE)
        .getAsStatement()
    );
  }

  @Test
  void testLambda() {
    final @JavaStatements String simpleLambdaParam = """
        builder.requires(source -> source.hasPermission("testcommand.use"));
        """;
    check(simpleLambdaParam, Builders.methodInvocation("requires")
        .setInstanceVariable("builder")
        .addParameter(CodeExpression.lambda(List.of("source"), Builders.methodInvocation("hasPermission")
            .setInstanceVariable("source")
            .addParameter(CodeExpression.string("testcommand.use"))
        ))
        .getAsStatement()
    );

    final @JavaStatements String simpleMultilineLambdaParam = """
        builder.executes(ctx -> {
          instance.run(ctx.getSource());
          return;
        });
        """;
    check(simpleMultilineLambdaParam, Builders.methodInvocation("executes")
        .setInstanceVariable("builder")
        .addParameter(CodeExpression.lambda(List.of("ctx"),
            Builders.methodInvocation("run")
                .setInstanceVariable("instance")
                .addParameter(Builders.methodInvocation("getSource").setInstanceVariable("ctx")),
            CodeStatement.returnStatement(null)
        ))
        .getAsStatement()
    );

    final @JavaStatements String singleStatementMultilineLambdaParam = """
        launchTask(() -> {
          run("Second");
        }, "First");
        """;
    check(singleStatementMultilineLambdaParam, Builders.methodInvocation("launchTask")
        .addParameter(CodeExpression.lambda(List.of(),
            Builders.methodInvocation("run")
                .addParameter(CodeExpression.string("Second"))
                .getAsStatement()
        ))
        .addParameter(CodeExpression.string("First"))
        .getAsStatement()
    );

    final @JavaStatements String testMultiParam = """
        sortBy((a, b) -> Integer.compare(a, b));
        """;
    check(testMultiParam, Builders.methodInvocation("sortBy")
        .addParameter(CodeExpression.lambda(List.of("a", "b"),
            Builders.methodInvocation("compare")
                .setStatic()
                .setType(CodeType.ofClass("java.lang.Integer"))
                .addParameter(CodeExpression.variable("a"))
                .addParameter(CodeExpression.variable("b"))
        ))
        .getAsStatement()
    );
  }

  @Test
  void testIfStatement() {
    final @JavaStatements String expected = """
        if (ctx.getSource() instanceof Player source) {
          source.sendPlainMessage("Hello!");
        }
        """;
    check(expected, CodeStatement.ifStmt(
        CodeExpression.instanceofExpr(
            Builders.methodInvocation("getSource").setInstanceVariable("ctx"),
            CodeType.ofClass("org.bukkit.entity.Player"),
            "source"
        ),
        Builders.methodInvocation("sendPlainMessage")
            .setInstanceVariable("source")
            .addParameter(CodeExpression.string("Hello!"))
    ));

    final @JavaStatements String expectedInverted = """
        if (!(ctx.getSource() instanceof Player)) {
          throw new IllegalStateException("Don't do that.");
        }
        """;
    check(expectedInverted, CodeStatement.ifStmt(
        CodeExpression.instanceofExpr(
            Builders.methodInvocation("getSource").setInstanceVariable("ctx"),
            CodeType.ofClass("org.bukkit.entity.Player"),
            null
        ).invert(),
        CodeStatement.throwStatement(Builders.ctorInvocation(CodeType.ofClass("java.lang.IllegalStateException"))
            .addParameter(CodeExpression.string("Don't do that."))
        )
    ));

    final @JavaStatements String expectedWithNestedCtor = """
        if (!(ctx.getSource() instanceof Player)) {
          throw new SimpleCommandExceptionType(
            new LiteralMessage("This command requires a player sender!")
          ).create();
        }
        """;
    check(expectedWithNestedCtor, CodeStatement.ifStmt(
        CodeExpression.instanceofExpr(
            Builders.methodInvocation("getSource").setInstanceVariable("ctx"),
            CodeType.ofClass("org.bukkit.entity.Player"),
            null
        ).invert(),
        CodeStatement.throwStatement(Builders.ctorInvocation(CodeType.ofClass("brigadier.SimpleCommandExceptionType"))
            .setMultilineParameters()
            .addParameter(Builders.ctorInvocation(CodeType.ofClass("brigadier.LiteralMessage"))
                .addParameter(CodeExpression.string("This command requires a player sender!"))
            )
            .chain("create")
        )
    ));
  }

  @Test
  void testFieldAccess() {
    check("value", Builders.fieldAccess("value").build());
    check("inst.value", Builders.fieldAccess("value").setSource(CodeExpression.variable("inst")).build());
    check("inst.value", Builders.fieldAccess("value")
        .setSource(CodeExpression.variable("inst"))
        .setType(CodeType.ofClass("some.ClassType"))
        .build());
    check("value", Builders.fieldAccess("value")
        .setType(CodeType.ofClass("some.ClassType"))
        .build());

    check("ClassType.value", Builders.fieldAccess("value")
        .setStatic(CodeType.ofClass("some.ClassType"))
        .build()
    );
    check("ClassType.fetch().value", Builders.fieldAccess("value")
        .setSource(Builders.methodInvocation("fetch")
            .setStatic()
            .setType(CodeType.ofClass("another.ClassType")))
        .build()
    );

    check("Yet.another.value", Builders.fieldAccess("value")
        .setSource(Builders.fieldAccess("another")
            .setStatic(CodeType.ofClass("yet.Yet"))
        )
        .build()
    );
  }

  @Test
  void testFullExecutesMethod() {
    final @JavaStatements String expected = """
        builder.executes(ctx -> {
          if (!(ctx.getSource() instanceof Player source)) {
            throw new SimpleCommandExceptionType(
              new LiteralMessage("This command requires a player sender!")
            ).create();
          }
        
          instance.run(source);
          return Command.SINGLE_SUCCESS;
        });
        """;
    check(expected, Builders.methodInvocation("executes")
        .setInstanceVariable("builder")
        .addParameter(CodeExpression.lambda(
            List.of("ctx"),
            CodeStatement.ifStmt(
                CodeExpression.instanceofExpr(
                    Builders.methodInvocation("getSource").setInstanceVariable("ctx"),
                    CodeType.ofClass("bukkit.Player"),
                    "source"
                ).invert(),
                CodeStatement.throwStatement(
                    Builders.ctorInvocation(CodeType.ofClass("brigadier.SimpleCommandExceptionType"))
                        .setMultilineParameters()
                        .addParameter(Builders.ctorInvocation(CodeType.ofClass("brigadier.LiteralMessage"))
                            .addParameter(CodeExpression.string("This command requires a player sender!")))
                        .chain("create")
                )
            ),
            CodeStatement.blank(),
            Builders.methodInvocation("run")
                .setInstanceVariable("instance")
                .addParameter(CodeExpression.variable("source")),
            CodeStatement.returnStatement(Builders.fieldAccess("SINGLE_SUCCESS")
                .setStatic(CodeType.ofClass("brigadier.Command"))
            )
        ))
        .getAsStatement()
    );
  }

  @Test
  void testAdvancedRegisterMethod() {
    // language=java
    final String expected = """
        class BrigadierTest {
        
          public void register(ProxyServer server, Object command$plugin) {
            final BrigadierCommand command = new BrigadierCommand(create());
            final CommandMeta meta = server.getCommandManager().metaBuilder(command)
              .aliases(ALIASES.toArray(String[]::new))
              .plugin(command$plugin)
              .build();
        
            server.getCommandManager().register(meta, command);
          }
        }
        """;
    check(expected, Builders.classBuilder("pkg.BrigadierTest")
        .addMethod(Builders.method("register")
            .setModifiers(Set.of(Modifiers.PUBLIC))
            .addParameter(CodeType.ofClass("velocity.ProxyServer"), "server")
            .addParameter(CodeType.ofClass("java.lang.Object"), "command$plugin")
            .setMethodStatements(List.of(
                // BrigadierCommand command
                CodeStatement.variableDeclarationFinal(
                    CodeType.ofClass("velocity.BrigadierCommand"),
                    "command",
                    Builders.ctorInvocation(CodeType.ofClass("velocity.BrigadierCommand"))
                        .addParameter(Builders.methodInvocation("create"))
                ),

                // CommandMeta meta
                CodeStatement.variableDeclarationFinal(
                    CodeType.ofClass("velocity.CommandMeta"),
                    "meta",
                    Builders.methodInvocation("getCommandManager")
                        .setInstanceVariable("server")
                        .chain("metaBuilder", CodeExpression.variable("command"))
                        .chain("aliases", InvokesMethod.StyleConfig.NEWLINE, Builders.methodInvocation("toArray")
                            .setInstanceVariable("ALIASES")
                            .addParameter(CodeExpression.methodReference(CodeType.STRING_ARRAY, "new"))
                        )
                        .chain("plugin", InvokesMethod.StyleConfig.NEWLINE, CodeExpression.variable("command$plugin"))
                        .chain("build", InvokesMethod.StyleConfig.NEWLINE)
                ),

                CodeStatement.blank(),

                // register call
                Builders.methodInvocation("getCommandManager")
                    .setInstanceVariable("server")
                    .chain("register", CodeExpression.variable("meta"), CodeExpression.variable("command"))
            ))
        )
        .build()
    );
  }
}
