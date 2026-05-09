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
import net.strokkur.commands.internal.printer.command.JavaSourcePrintingVisitor;
import net.strokkur.commands.internal.printer.javadoc.JavaMarkdownJavadocVisitor;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaCodeGenTests {
  private static final CodeClass EXAMPLE_CLASS = Builders.classBuilder("com.example.ExampleClass").build();

  private void check(@Language("JAVA") String expected, CodeVisitable visitable) {
    final JavaSourcePrintingVisitor visitor = new JavaSourcePrintingVisitor(JavaMarkdownJavadocVisitor::new, "  ");
    final String actual = visitable.accept(visitor).toString();
    assertEquals(expected, actual);
  }

  @Test
  void testPackageThrows() {
    assertThrows(IllegalStateException.class, () -> {
      CodePackage.of("com.example").accept(new JavaSourcePrintingVisitor(JavaMarkdownJavadocVisitor::new, "  "));
    });
  }

  @Test
  void testClass() {
    final @Language("JAVA") String expected = """
        class ExampleClass {
        }
        """;
    check(expected, EXAMPLE_CLASS);

    final @Language("JAVA") String expectedWithFields = """
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

    final @Language("JAVA") String expectedWithMethods = """
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

    final @Language("JAVA") String expectedCombined = """
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
        .addMethod(Builders.method()
            .setDeclaringClass(EXAMPLE_CLASS)
            .buildConstructor()
        )
        .build()
    );
  }

  @Test
  void testMethod() {
    final @Language("JAVA") String expected = """
        void method() {
        }
        """;
    check(expected, Builders.method(EXAMPLE_CLASS, "method").build());

    final @Language("JAVA") String expectedWithModifiers = """
        public static void method() {
        }
        """;
    check(expectedWithModifiers, Builders.method(EXAMPLE_CLASS, "method")
        .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.STATIC))
        .build());

    final @Language("JAVA") String expectedWithStatements = """
        void method() {
          otherMethod(STATIC_VALUE, "Now");
        }
        """;
    check(expectedWithStatements, Builders.method(EXAMPLE_CLASS, "method")
        .setCodeBlock(List.of(
            CodeStatement.methodInvocation(Builders.method(EXAMPLE_CLASS, "otherMethod").build(),
                List.of(
                    CodeExpression.variable("STATIC_VALUE"),
                    CodeExpression.string("Now")
                )
            )
        ))
        .build());

    final @Language("JAVA") String expectedWithThrows = """
        void method() throws NullPointerException, SQLException {
          throw new SQLException("No database present :(");
        }
        """;
    check(expectedWithThrows, Builders.method(EXAMPLE_CLASS, "method")
        .setThrowsExceptions(List.of(
            CodeType.ofClass("java.lang.NullPointerException"),
            CodeType.ofClass("java.sql.SQLException")
        ))
        .setCodeBlock(List.of(
            CodeStatement.throwStatement(CodeExpression.constructorCall(
                CodeType.ofClass("java.sql.SQLException"), List.of(
                    CodeExpression.string("No database present :(")
                )
            ))
        ))
        .build());
  }

  @Test
  void testField() {
    final @Language("JAVA") String expectedNoInit = """
        String someField;
        """;
    check(expectedNoInit, Builders.field("someField", CodeType.ofClass(CodeClass.STRING)).build());

    final @Language("JAVA") String expectedWithInit = """
        String someField = "some value";
        """;
    check(expectedWithInit, Builders.field("someField", CodeType.ofClass(CodeClass.STRING))
        .setInitialiser(CodeExpression.string("some value"))
        .build());

    final @Language("JAVA") String expectedWithModifiers = """
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
}
