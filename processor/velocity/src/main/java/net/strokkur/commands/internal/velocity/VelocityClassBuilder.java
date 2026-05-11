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
package net.strokkur.commands.internal.velocity;

import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.InvokesMethod;
import net.strokkur.commands.internal.codegen.Modifiers;
import net.strokkur.commands.internal.codegen.adapter.CodeTypeAdapter;
import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.codegen.builder.ClassBuilder;
import net.strokkur.commands.internal.codegen.builder.MethodBuilder;
import net.strokkur.commands.internal.codegen.builder.MethodInvocationBuilder;
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonClassBuilder;
import net.strokkur.commands.internal.printer.source.AbstractSourcePrintingVisitor;
import net.strokkur.commands.internal.util.ConvertableTo;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.internal.velocity.util.VelocityCommandInformation;

import java.util.Set;
import java.util.function.BiFunction;

class VelocityClassBuilder extends CommonClassBuilder<VelocityCommandInformation> {
  VelocityClassBuilder(
      CommandNode rootNode,
      VelocityCommandInformation commandInformation,
      BiFunction<CodePackage, Set<CodeType.ClassType>, AbstractSourcePrintingVisitor> sourceVisitor
  ) {
    super(rootNode, commandInformation, new VelocityBrigadierStatementBuilder(), sourceVisitor);
  }

  @Override
  protected MethodBuilder getCreateMethodBuilder() {
    return super.getCreateMethodBuilder()
        .setReturnType(VelocityClasses.TYPED_LITERAL_COMMAND_NODE.getAsCodeType());
  }

  @Override
  protected void populateStaticFields(ClassBuilder builder) {
    super.populateStaticFields(builder);

    final MethodInvocationBuilder listOf = Builders.methodInvocation("of").setStatic(CodeType.LIST);
    if (commandInformation.aliases() != null) {
      for (String alias : commandInformation.aliases()) {
        listOf.addParameter(CodeExpression.string(alias));
      }
    }

    builder.addField(Builders.field("ALIASES", CodeType.LIST_STRING)
        .setModifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
        .setInitialiser(listOf)
    );
  }

  @Override
  protected MethodBuilder getRegisterMethodBuilder() {
    final MethodInvocationBuilder createInvocation = Builders.methodInvocation("create");

    if (!commandInformation.useInjection()) {
      if (commandInformation.constructor() instanceof SourceConstructor sourceCtor) {
        for (SourceParameter parameter : sourceCtor.getParameters()) {
          final String variableName;
          if (isProxyServer(parameter)) {
            variableName = "server";
          } else {
            variableName = parameter.getName();
          }

          createInvocation.addParameter(CodeExpression.variable(variableName));
        }
      }
    }

    final MethodBuilder builder = super.getRegisterMethodBuilder()
        .addParameter(VelocityClasses.PROXY_SERVER.getAsCodeType(), "server")
        .addParameter(CodeType.OBJECT, "command$plugin")
        .setMethodStatements(
            CodeStatement.variableDeclarationFinal(VelocityClasses.BRIGADIER_COMMAND, "command", Builders.ctorInvocation(VelocityClasses.BRIGADIER_COMMAND)
                .addParameter(createInvocation)),
            CodeStatement.variableDeclarationFinal(VelocityClasses.COMMAND_META, "meta", Builders.methodInvocation("getCommandManager")
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
        );

    addConstructorParametersTo(builder, f -> !isProxyServer(f));
    return builder;
  }

  private boolean isProxyServer(SourceVariable sourceVar) {
    return CodeTypeAdapter.from(sourceVar.getType()).equals(VelocityClasses.PROXY_SERVER.getAsCodeType());
  }

  @Override
  protected void applyRegisterMethodJavadoc(MethodBuilder registerMethod, ConvertableTo<CodeMethod> createMethod) {
    registerMethod.setJavadoc(CodeJavadoc.combineLines(
        CodeJavadoc.text("Shortcut for registering the command node returned from"),
        CodeJavadoc.combine(CodeJavadoc.methodReference(createMethod, true), CodeJavadoc.text(". This method uses the provided aliases")),
        CodeJavadoc.text("from the original source file."),

        CodeJavadoc.header("Registering the command", 3),

        CodeJavadoc.combine(
            CodeJavadoc.text("Commands should only be registered during the "),
            CodeJavadoc.classReference(VelocityClasses.PROXY_INITIALIZE_EVENT.getAsCodeType().codeClass()),
            CodeJavadoc.text(".")
        ),
        CodeJavadoc.text("The example below shows an example of how to do this. For more information,"),
        CodeJavadoc.combine(CodeJavadoc.text("refer to "), CodeJavadoc.url("The Velocity Command API docs", "https://docs.papermc.io/velocity/dev/command-api/#registering-a-command")),

        CodeJavadoc.blank(),

        CodeJavadoc.codeBlock("""
            @Subscribe
            void onProxyInitialize(final ProxyInitializeEvent event) {
              %s.register(this.proxy, this);
            }""".formatted(selfType.name()))
    ));
  }
}
