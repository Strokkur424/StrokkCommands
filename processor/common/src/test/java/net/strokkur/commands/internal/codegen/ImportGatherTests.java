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
import net.strokkur.commands.internal.printer.command.ImportGatheringVisitor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportGatherTests {

  private void check(String expectedMultiline, CodeVisitable visitable) {
    final Set<String> imports = visitable.accept(new ImportGatheringVisitor());

    if (expectedMultiline.isBlank()) {
      assertEquals(0, imports.size());
      return;
    }

    final Set<String> expected = Arrays.stream(expectedMultiline.strip().split("\n"))
        .collect(Collectors.toSet());
    assertLinesMatch(expected.stream().sorted(), imports.stream().sorted());
  }

  @Test
  void testPackageVisitThrows() {
    assertThrows(IllegalStateException.class, () -> {
      CodePackage.of("com.example").accept(new ImportGatheringVisitor());
    });
  }

  @Test
  void testGatherExpressionImports() {
    // Test static method invocation
    check("""
        com.example.Test""", Builders.methodInvocation(Builders.method(CodeClass.simple("com.example.Test"), "execute")
            .setModifiers(Set.of(Modifiers.STATIC)))
        .getAsExpression()
    );

    // Test static method invocation with parameters
    check("""
        com.example.Test
        io.library.Value
        """, Builders.methodInvocation(Builders.method(CodeClass.simple("com.example.Test"), "execute")
            .setModifiers(Set.of(Modifiers.STATIC)))
        .addParameter(CodeExpression.constructorCall(CodeType.ofClass("io.library.Value")))
        .getAsExpression()
    );

    // Test instance method invocation
    check("", Builders.methodInvocation(Builders.method(CodeClass.simple("com.example.Test"), "execute"))
        .setInstanceVariable("this")
        .getAsExpression()
    );

    // Test instance method invocation with parameters
    check("""
        io.library.Value
        """, Builders.methodInvocation(Builders.method(CodeClass.simple("com.example.Test"), "execute"))
        .addParameter(CodeExpression.constructorCall(CodeType.ofClass("io.library.Value")))
        .setInstanceVariable("this")
        .getAsExpression()
    );

    // Test method reference
    check(
        "io.library.Example",
        CodeExpression.methodReference(CodeType.ofClass("io.library.Example"), "run")
    );
  }

  @Test
  void testFields() {
    // No initializer
    check("""
        java.lang.String
        """, Builders.field("field", CodeType.ofClass(CodeClass.STRING))
        .build()
    );

    // With initializer
    check("""
        java.lang.String
        com.example.TestClass
        """, Builders.field("field", CodeType.ofClass(CodeClass.STRING))
        .setInitialiser(
            Builders.methodInvocation(Builders.method(CodeClass.simple("com.example.TestClass"), "get")
                .setModifiers(Set.of(Modifiers.STATIC))
            )
        )
        .build()
    );
  }

  @Test
  void testStatements() {
    // Variable declaration (no init)
    check("""
        java.lang.String
        """, CodeStatement.variableDeclaration(
        CodeType.ofClass(CodeClass.STRING),
        "name",
        null
    ));

    // Variable declaration (with init)
    check("""
        java.lang.String
        com.example.TheClass
        """, CodeStatement.variableDeclaration(
        CodeType.ofClass(CodeClass.STRING),
        "name",
        Builders.methodInvocation(Builders.method(CodeClass.simple("com.example.TheClass"), "get")
            .setModifiers(Set.of(Modifiers.STATIC))
        )
    ));

    // Return statement (no value)
    check("", CodeStatement.returnStatement(null));
    // Return statement (with value)
    check("", CodeStatement.returnStatement(
        CodeExpression.nullExpr()
    ));

    // Throw statement
    check("java.lang.NullPointerException", CodeStatement.throwStatement(
        CodeExpression.constructorCall(
            CodeType.ofClass("java.lang.NullPointerException"),
            CodeExpression.string("It was null :(")
        )
    ));

    // Method invocation (instance)
    check("", Builders.methodInvocation(Builders.method(CodeClass.simple("io.declared.ThisClass"), "doSomething"))
        .addParameter(CodeExpression.string("test"))
        .getAsStatement()
    );
    // Method invocation (static)
    check("io.declared.ThisClass", Builders.methodInvocation(Builders.method(CodeClass.simple("io.declared.ThisClass"), "doSomething")
            .setModifiers(Set.of(Modifiers.STATIC)))
        .addParameter(CodeExpression.string("test"))
        .getAsStatement()
    );
  }

  @Test
  void testArray() {
    check("io.library.TestClass", CodeType.ofArray(CodeType.ofClass("io.library.TestClass")));
    check("java.lang.String", CodeType.STRING_ARRAY);
    // String[][]
    check("java.lang.String", CodeType.ofArray(CodeType.STRING_ARRAY));
  }

  @Test
  void testLambda() {
    check("io.library.SomeTest", CodeExpression.lambda(List.of(), Builders.methodInvocation("do")
        .setStatic()
        .setType(CodeType.ofClass("io.library.SomeTest"))
    ));
    check("""
        io.library.SomeTest
        io.library.AnotherTest
        """, CodeExpression.lambda(List.of(),
        Builders.methodInvocation("do")
            .setStatic()
            .setType(CodeType.ofClass("io.library.SomeTest")),
        Builders.methodInvocation("anotherOne")
            .setStatic()
            .setType(CodeType.ofClass("io.library.AnotherTest"))
    ));
  }

  @Test
  void testIfStatement() {
    // With instanceof
    check("org.bukkit.entity.Player", CodeStatement.ifStmt(
        CodeExpression.instanceofExpr(
            Builders.methodInvocation("getSource").setInstanceVariable("ctx"),
            CodeType.ofClass("org.bukkit.entity.Player"),
            "source"
        ),
        Builders.methodInvocation("sendPlainMessage")
            .setInstanceVariable("source")
            .addParameter(CodeExpression.string("Hello!"))
    ));

    check("""
        java.lang.IllegalStateException
        io.library.StaticLibrary
        org.bukkit.entity.Player
        """, CodeStatement.ifStmt(
        CodeExpression.instanceofExpr(
            Builders.methodInvocation("getSource")
                .setStatic()
                .setType(CodeType.ofClass("io.library.StaticLibrary")),
            CodeType.ofClass("org.bukkit.entity.Player"),
            null
        ).invert(),
        CodeStatement.throwStatement(CodeExpression.constructorCall(
            CodeType.ofClass("java.lang.IllegalStateException"),
            CodeExpression.string("Don't do that.")
        ))
    ));
  }

  @Test
  void testBlank() {
    check("", CodeStatement.blank());
  }
}
