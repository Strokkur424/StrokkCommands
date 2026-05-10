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
import net.strokkur.commands.internal.printer.javadoc.AbstractJavadocPrintingVisitor;

import java.util.function.Supplier;

import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.author;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.blank;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.classReference;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.codeBlock;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.combine;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.combineLines;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.header;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.inlineCode;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.linebreak;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.methodReference;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.see;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.text;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.throwsMeta;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.url;
import static net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc.version;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class CommonJavadocVisitorTests {
  void checkOutput(String expected, CodeJavadoc javadoc, Supplier<AbstractJavadocPrintingVisitor> visitorSupplier) {
    final AbstractJavadocPrintingVisitor visitor = visitorSupplier.get();
    javadoc.accept(visitor);
    final String actual = String.join("\n", visitor.getLines());
    assertEquals(expected, actual);
  }

  CodeJavadoc classJavadoc() {
    return combineLines(
        text("A class holding the Brigadier source tree generated from"),
        combine(classReference(sourceClass()), text(" using "), url("StrokkCommands", "https://commands.strokkur.net")),
        blank(),
        author("Strokkur24 - StrokkCommands"),
        version("2.0.0"),
        see(createMethod(), "creating the LiteralCommandNode", true),
        see(registerMethod(), "registering the LiteralCommandNode", true)
    );
  }

  CodeJavadoc registerJavadoc() {
    return combineLines(
        text("Shortcut for registering the command node returned from"),
        combine(methodReference(createMethod(), true), text(". This method uses the provided aliases")),
        text("and description from the original source file."),
        header("Registering the command", 3),
        text("This method can safely be called either in your plugin bootstrapper's"),
        combine(methodReference(bootstrapMethod()), text(" or your main")),
        combine(text("class' "), methodReference(onLoadMethod()), text(" or "), methodReference(onEnableMethod())),
        text("methods."),
        linebreak(),
        text("You need to call it inside of a lifecycle event. General information can be found on the"),
        combine(url("PaperMC Lifecycle API docs page", "https://docs.papermc.io/paper/dev/lifecycle/"), text(".")),
        linebreak(),
        combine(text("The general use case might look like this (example given inside the "), inlineCode("onEnable"), text(" method):")),
        codeBlock("""
            this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
                final Commands commands = event.registrar();
                EntitiesCommandBrigadier.register(commands);
            }""")
    );
  }

  CodeJavadoc createJd() {
    return combineLines(
        text("A method for creating a Brigadier command node which denotes the declared command"),
        combine(text("in "), classReference(sourceClass()), text(". "), text("You can either retrieve the unregistered node with this method")),
        combine(text("or register it directly with "), methodReference(registerMethod(), true), text("."))
    );
  }

  CodeJavadoc ctorJd() {
    return combineLines(
        text("The constructor is not accessible. There is no need for an instance"),
        text("to be created, as no state is stored and all methods are static."),
        blank(),
        throwsMeta(CodeType.ofClass("java.lang.IllegalAccessException"), "always")
    );
  }

  CodeClass sourceClass() {
    return CodeClass.simple("com.example.CommandClass");
  }

  CodeClass targetClass() {
    return CodeClass.simple("com.example.CommandClassBrigadier");
  }

  CodeMethod createMethod() {
    return Builders.method(targetClass(), "create")
        .build();
  }

  CodeMethod registerMethod() {
    return Builders.method(targetClass(), "register")
        .addParameter(CodeType.ofClass(CodeClass.simple("io.papermc.paper.command.brigadier.Commands")), "commands")
        .build();
  }

  CodeMethod bootstrapMethod() {
    return Builders.method("bootstrap")
        .setDeclaringClass(CodeClass.simple("io.papermc.paper.plugin.bootstrap.PluginBootstrap"))
        .addParameter(CodeType.ofClass(CodeClass.simple("io.papermc.paper.plugin.bootstrap.BootstrapContext")), "context")
        .build();
  }

  CodeMethod onLoadMethod() {
    return Builders.method("onLoad")
        .setDeclaringClass(CodeClass.simple("org.bukkit.plugin.java.JavaPlugin"))
        .build();
  }

  CodeMethod onEnableMethod() {
    return Builders.method("onEnable")
        .setDeclaringClass(CodeClass.simple("org.bukkit.plugin.java.JavaPlugin"))
        .build();
  }
}
