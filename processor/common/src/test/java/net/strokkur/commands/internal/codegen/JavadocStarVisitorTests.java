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
import net.strokkur.commands.internal.printer.javadoc.JavaStarJavadocVisitor;
import org.junit.jupiter.api.Test;

import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.classReference;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.combine;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.combineLines;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.methodReference;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.text;

class JavadocStarVisitorTests extends CommonJavadocVisitorTests {
  @Test
  void testJavaStarJavadocsClass() {
    // language=java
    final String expected = """
        /**
         * A class holding the Brigadier source tree generated from
         * {@link com.example.CommandClass} using <a href="https://commands.strokkur.net">StrokkCommands</a>
         *
         * @author Strokkur24 - StrokkCommands
         * @version 2.0.0
         * @see #create() creating the LiteralCommandNode
         * @see #register(io.papermc.paper.command.brigadier.Commands) registering the LiteralCommandNode
         */""";
    checkOutput(expected, classJavadoc(), JavaStarJavadocVisitor::new);
  }

  @Test
  void testJavaStarJavadocsCreate() {
    // language=java
    final String expected = """
        /**
         * A method for creating a Brigadier command node which denotes the declared command
         * in {@link com.example.CommandClass}. You can either retrieve the unregistered node with this method
         * or register it directly with {@link #register(io.papermc.paper.command.brigadier.Commands)}.
         */""";
    checkOutput(expected, createJd(), JavaStarJavadocVisitor::new);
  }

  @Test
  void testJavaStarJavadocsRegister() {
    // language=java
    final String expected = """
        /**
         * Shortcut for registering the command node returned from
         * {@link #create()}. This method uses the provided aliases
         * and description from the original source file.
         *
         * <h3>Registering the command</h3>
         *
         * This method can safely be called either in your plugin bootstrapper's
         * {@link io.papermc.paper.plugin.bootstrap.PluginBootstrap#bootstrap(io.papermc.paper.plugin.bootstrap.BootstrapContext)} or your main
         * class' {@link org.bukkit.plugin.java.JavaPlugin#onLoad()} or {@link org.bukkit.plugin.java.JavaPlugin#onEnable()}
         * methods.
         * <p>
         * You need to call it inside of a lifecycle event. General information can be found on the
         * <a href="https://docs.papermc.io/paper/dev/lifecycle/">PaperMC Lifecycle API docs page</a>.
         * <p>
         * The general use case might look like this (example given inside the {@code onEnable} method):
         * <pre>{@code
         * this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
         *     final Commands commands = event.registrar();
         *     EntitiesCommandBrigadier.register(commands);
         * }
         * }</pre>
         */""";
    checkOutput(expected, registerJavadoc(), JavaStarJavadocVisitor::new);
  }

  @Test
  void testJavaStarJavadocsCtor() {
    // language=java
    final String expected = """
        /**
         * The constructor is not accessible. There is no need for an instance
         * to be created, as no state is stored and all methods are static.
         *
         * @throws java.lang.IllegalAccessException always
         */""";
    checkOutput(expected, ctorJd(), JavaStarJavadocVisitor::new);
  }

  @Test
  void testNamedClassReference() {
    // language=java
    final String expected = """
        /**
         * This {@link java.lang.ProcessEnvironment environment} does not help me
         * at all.
         */""";
    checkOutput(expected, combineLines(
        combine(text("This "), classReference(CodeClass.simple("java.lang.ProcessEnvironment"), "environment"), text(" does not help me")),
        text("at all.")
    ), JavaStarJavadocVisitor::new);
  }

  @Test
  void testNamedMethodReference() {
    // language=java
    final String expected = """
        /**
         * Use the {@link #builder() builder} for quick access.
         */""";
    checkOutput(
        expected,
        combine(
            text("Use the "),
            methodReference(Builders.method(CodeClass.simple("none.None"), "builder").build(), "builder", true),
            text(" for quick access.")
        ),
        JavaStarJavadocVisitor::new
    );
  }
}
