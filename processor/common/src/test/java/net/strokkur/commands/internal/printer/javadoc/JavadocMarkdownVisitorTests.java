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
package net.strokkur.commands.internal.printer.javadoc;

import org.junit.jupiter.api.Test;

class JavadocMarkdownVisitorTests extends CommonJavadocVisitorTests {
  @Test
  void testJavaMarkdownJavadocsClass() {
    // language=java
    final String expected = """
        /// A class holding the Brigadier source tree generated from
        /// [com.example.CommandClass] using [StrokkCommands](https://commands.strokkur.net)
        ///
        /// @author Strokkur24 - StrokkCommands
        /// @version 2.0.0
        /// @see #create() creating the LiteralCommandNode
        /// @see #register(io.papermc.paper.command.brigadier.Commands) registering the LiteralCommandNode""";
    checkOutput(expected, classJavadoc(), JavaMarkdownJavadocVisitor::new);
  }

  @Test
  void testJavaMarkdownJavadocsCreate() {
    // language=java
    final String expected = """
        /// A method for creating a Brigadier command node which denotes the declared command
        /// in [com.example.CommandClass]. You can either retrieve the unregistered node with this method
        /// or register it directly with [#register(io.papermc.paper.command.brigadier.Commands)].""";
    checkOutput(expected, createJd(), JavaMarkdownJavadocVisitor::new);
  }

  @Test
  void testJavaMarkdownJavadocsRegister() {
    // language=java
    final String expected = """
        /// Shortcut for registering the command node returned from
        /// [#create()]. This method uses the provided aliases
        /// and description from the original source file.
        ///
        /// ### Registering the command
        ///
        /// This method can safely be called either in your plugin bootstrapper's
        /// [io.papermc.paper.plugin.bootstrap.PluginBootstrap#bootstrap(io.papermc.paper.plugin.bootstrap.BootstrapContext)] or your main
        /// class' [org.bukkit.plugin.java.JavaPlugin#onLoad()] or [org.bukkit.plugin.java.JavaPlugin#onEnable()]
        /// methods.
        ///
        /// You need to call it inside of a lifecycle event. General information can be found on the
        /// [PaperMC Lifecycle API docs page](https://docs.papermc.io/paper/dev/lifecycle/).
        ///
        /// The general use case might look like this (example given inside the `onEnable` method):
        /// ```
        /// this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
        ///     final Commands commands = event.registrar();
        ///     EntitiesCommandBrigadier.register(commands);
        /// }
        /// ```""";
    checkOutput(expected, registerJavadoc(), JavaMarkdownJavadocVisitor::new);
  }

  @Test
  void testJavaMarkdownJavadocsCtor() {
    // language=java
    final String expected = """
        /// The constructor is not accessible. There is no need for an instance
        /// to be created, as no state is stored and all methods are static.
        ///
        /// @throws java.lang.IllegalAccessException always""";
    checkOutput(expected, ctorJd(), JavaMarkdownJavadocVisitor::new);
  }
}
