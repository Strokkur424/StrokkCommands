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
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import net.strokkur.commands.internal.printer.command.ImportGatheringVisitor;
import net.strokkur.commands.internal.printer.command.JavaSourcePrintingVisitor;
import net.strokkur.commands.internal.printer.javadoc.JavaMarkdownJavadocVisitor;
import net.strokkur.commands.internal.util.Classes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class FullCommandClassBuilderTests {

  @Test
  void testFullClassExampleImports() {
    final CodeClass exampleClass = constructExampleCommandClass();

    final ImportGatheringVisitor visitor = new ImportGatheringVisitor();
    final Set<String> collectedImports = exampleClass.accept(visitor);

    final String expected = """
        com.example.ExampleCommandBrigadier
        io.papermc.paper.command.brigadier.Commands
        java.lang.IllegalAccessException
        java.lang.String
        org.jspecify.annotations.NullMarked
        org.jspecify.annotations.Nullable""";
    final String actual = collectedImports.stream()
        .sorted()
        .collect(Collectors.joining("\n"));

    Assertions.assertEquals(expected, actual);
  }

  @Test
  void testFullClassExampleJavaPrinter() {
    final CodeClass exampleClass = constructExampleCommandClass();
    final JavaSourcePrintingVisitor visitor = new JavaSourcePrintingVisitor(JavaMarkdownJavadocVisitor::new, "  ");

    // language=java
    final String expected = """
        /// A very cool class.
        ///
        /// @author Strokkur24
        @NullMarked
        public final class ExampleCommandBrigadier {
          public static final String NAME = "example";
          public static final String DESCRIPTION = null;
          public static final String ALIASES = "example";
        
          /// Registers your command.
          public static void register(Commands commands) {
            commands.register(create(), DESCRIPTION, ALIASES);
          }
        
          public static void create() {
          }
        
          /// The constructor is inaccessible.
          ///
          /// @throws java.lang.IllegalAccessException always
          private ExampleCommandBrigadier() throws IllegalAccessException {
            throw new IllegalAccessException("This class cannot be instantiated.");
          }
        }
        """;
    final String actual = exampleClass.accept(visitor).toString();

    Assertions.assertEquals(expected, actual);
  }

  private CodeClass constructExampleCommandClass() {
    final CodeClass commands = CodeClass.simple("io.papermc.paper.command.brigadier.Commands");
    final CodeClass current = CodeClass.simple("com.example.ExampleCommandBrigadier");

    return Builders.classBuilder(current.fullyQualifiedName())
        .setAnnotations(List.of(CodeAnnotation.of(CodeType.ofClass(Classes.NULL_MARKED))))
        .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.FINAL))
        .setJavadoc(CodeJavadoc.combineLines(
            CodeJavadoc.text("A very cool class."),
            CodeJavadoc.linebreak(),
            CodeJavadoc.author("Strokkur24")
        ))
        .addField(Builders.field("NAME", CodeType.ofClass(CodeClass.STRING))
            .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL))
            .setInitialiser(CodeExpression.string("example"))
        )
        .addField(Builders.field("DESCRIPTION", CodeType.ofClass(CodeClass.STRING))
            .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL))
            .setInitialiser(CodeExpression.nullExpr())
            .addAnnotation(CodeAnnotation.of(CodeType.ofClass(Classes.NULLABLE)))
        )
        .addField(Builders.field("ALIASES", CodeType.ofClass(CodeClass.STRING))
            .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL))
            .setInitialiser(CodeExpression.string("example"))
        )
        .addMethod(Builders.method(current, "register")
            .setJavadoc(CodeJavadoc.text("Registers your command."))
            .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.STATIC))
            .addParameter(CodeType.ofClass(commands), "commands")
            .setCodeBlock(List.of(
                Builders.methodInvocation("register")
                    .addParameter(Builders.methodInvocation("create"))
                    .addParameter(CodeExpression.variable("DESCRIPTION"))
                    .addParameter(CodeExpression.variable("ALIASES"))
                    .setInstanceVariable("commands")
                )
            )
        )
        .addMethod(Builders.method(current, "create")
            .setModifiers(Set.of(Modifiers.PUBLIC, Modifiers.STATIC))
        )
        .addMethod(Builders.method(current)
            .setThrowsExceptions(List.of(CodeType.ofClass("java.lang.IllegalAccessException")))
            .setModifiers(Set.of(Modifiers.PRIVATE))
            .setJavadoc(CodeJavadoc.combineLines(
                CodeJavadoc.text("The constructor is inaccessible."),
                CodeJavadoc.blank(),
                CodeJavadoc.throwsMeta(CodeType.ofClass("java.lang.IllegalAccessException"), "always")
            ))
            .setCodeBlock(List.of(
                CodeStatement.throwStatement(Builders.ctorInvocation(CodeType.ofClass("java.lang.IllegalAccessException"))
                    .addParameter(CodeExpression.string("This class cannot be instantiated."))
                )
            ))
            .buildConstructor()
        )
        .build();
  }
}
