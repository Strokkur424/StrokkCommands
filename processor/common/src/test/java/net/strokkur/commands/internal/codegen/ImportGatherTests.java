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
        com.example.Test""", CodeExpression.methodCall(
        Builders.method(CodeClass.simple("com.example.Test"), "execute")
            .setModifiers(Set.of(Modifiers.STATIC))
            .build(),
        List.of()
    ));

    // Test static method invocation with parameters
    check("""
        com.example.Test
        io.library.Value
        """, CodeExpression.methodCall(
        Builders.method(CodeClass.simple("com.example.Test"), "execute")
            .setModifiers(Set.of(Modifiers.STATIC))
            .build(),
        List.of(CodeExpression.constructorCall(CodeType.ofClass("io.library.Value"), List.of()))
    ));

    // Test instance method invocation
    check("", CodeExpression.methodCall(
        Builders.method(CodeClass.simple("com.example.Test"), "execute")
            .build(),
        List.of(),
        "this"
    ));

    // Test instance method invocation with parameters
    check("""
        io.library.Value
        """, CodeExpression.methodCall(
        Builders.method(CodeClass.simple("com.example.Test"), "execute").build(),
        List.of(CodeExpression.constructorCall(CodeType.ofClass("io.library.Value"), List.of())),
        "this"
    ));
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
        .setInitialiser(CodeExpression.methodCall(
            Builders.method(CodeClass.simple("com.example.TestClass"), "get")
                .setModifiers(Set.of(Modifiers.STATIC))
                .build(),
            List.of()
        ))
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
        CodeExpression.methodCall(
            Builders.method(CodeClass.simple("com.example.TheClass"), "get")
                .setModifiers(Set.of(Modifiers.STATIC))
                .build(),
            List.of()
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
            List.of(CodeExpression.string("It was null :("))
        )
    ));

    // Method invocation (instance)
    check("", CodeStatement.methodInvocation(
        Builders.method(CodeClass.simple("io.declared.ThisClass"), "doSomething").build(),
        List.of(
            CodeExpression.string("test")
        )
    ));
    // Method invocation (static)
    check("io.declared.ThisClass", CodeStatement.methodInvocation(
        Builders.method(CodeClass.simple("io.declared.ThisClass"), "doSomething")
            .setModifiers(Set.of(Modifiers.STATIC))
            .build(),
        List.of(
            CodeExpression.string("test")
        )
    ));
  }
}
