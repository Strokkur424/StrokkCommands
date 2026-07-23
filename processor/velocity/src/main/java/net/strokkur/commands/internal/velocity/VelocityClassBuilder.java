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

import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.CommonClassBuilder;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;
import net.strokkur.commands.internal.velocity.util.VelocityCommandInformation;
import net.strokkur.jap.code.classmodel.CodeField;
import net.strokkur.jap.code.classmodel.builder.ClassBuilder;
import net.strokkur.jap.code.classmodel.builder.MethodBuilder;
import net.strokkur.jap.code.convert.ConvertToMethod;
import net.strokkur.jap.code.documentation.CodeDocumentation;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.MethodInvocationBuilder;
import net.strokkur.jap.code.statement.Statements;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.code.util.Modifiers;
import net.strokkur.jap.code.util.StyleConfig;
import net.strokkur.jap.source.classmodel.SourceConstructor;
import net.strokkur.jap.source.classmodel.SourceMethodParameter;
import net.strokkur.jap.source.classmodel.SourceParameterLike;

class VelocityClassBuilder extends CommonClassBuilder<VelocityCommandInformation> {
  VelocityClassBuilder(CommandNode rootNode, VelocityCommandInformation commandInformation) {
    super(rootNode, commandInformation);
  }

  @Override
  protected MethodBuilder getCreateMethodBuilder() {
    return super.getCreateMethodBuilder()
      .setReturnType(Classes.LITERAL_COMMAND_NODE.typed(VelocityClasses.COMMAND_SOURCE));
  }

  @Override
  protected void populateStaticFields(ClassBuilder builder) {
    super.populateStaticFields(builder);

    final MethodInvocationBuilder listOf = JavaTypes.LIST.chainMethod("of");
    if (commandInformation.aliases() != null) {
      for (String alias : commandInformation.aliases()) {
        listOf.addParameters(Expressions.string(alias));
      }
    }

    builder.addFields(CodeField.builder(JavaTypes.LIST.typed(JavaTypes.STRING), "ALIASES")
      .addModifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
      .setInitializer(listOf)
    );
  }

  @Override
  protected MethodBuilder getRegisterMethodBuilder() {
    final MethodInvocationBuilder createInvocation = new MethodInvocationBuilder()
      .setName("create");

    if (!commandInformation.useInjection()) {
      if (commandInformation.constructor() instanceof SourceConstructor sourceCtor) {
        for (SourceMethodParameter parameter : sourceCtor.parameters()) {
          final String variableName;
          if (isProxyServer(parameter)) {
            variableName = "server";
          } else {
            variableName = parameter.name();
          }

          createInvocation.addParameters(Expressions.variable(variableName));
        }
      }
    }

    final MethodBuilder builder = super.getRegisterMethodBuilder()
      .addParameter(VelocityClasses.PROXY_SERVER, "server")
      .addParameter(JavaTypes.OBJECT, "command$plugin")
      .setCode(
        Statements.variableDeclarationFinal(VelocityClasses.BRIGADIER_COMMAND, "command", VelocityClasses.BRIGADIER_COMMAND.ctor(createInvocation)),

        Statements.variableDeclarationFinal(VelocityClasses.COMMAND_META, "meta", Expressions.variable("server")
          .chainMethod("getCommandManager")
          .chainMethod("metaBuilder", Expressions.variable("command"))
          .chainMethod("aliases", Expressions.variable("ALIASES").chainMethod("toArray")
            .addParameters(Expressions.methodReference(JavaTypes.STRING.toArray(), "new"))
          ).setStyle(StyleConfig.NEWLINE)
          .chainMethod("plugin", Expressions.variable("command$plugin")).setStyle(StyleConfig.NEWLINE)
          .chainMethod("build").setStyle(StyleConfig.NEWLINE)
        ),
        Statements.blank(),
        Expressions.variable("server")
          .chainMethod("getCommandManager")
          .chainMethod("register", Expressions.variable("meta"), Expressions.variable("command"))
      );

    addConstructorParametersTo(builder, f -> !isProxyServer(f));
    return builder;
  }

  private boolean isProxyServer(SourceParameterLike sourceVar) {
    return VelocityClasses.PROXY_SERVER.toClassType().equals(sourceVar.type().toType());
  }

  @Override
  protected void applyRegisterMethodJavadoc(MethodBuilder registerMethod, ConvertToMethod createMethod) {
    registerMethod.setDocumentation(CodeDocumentation.combineLines(
      CodeDocumentation.text("Shortcut for registering the command node returned from"),
      CodeDocumentation.combine(CodeDocumentation.methodReference(createMethod), CodeDocumentation.text(". This method uses the provided aliases")),
      CodeDocumentation.text("from the original source file."),

      CodeDocumentation.header("Registering the command", 3),

      CodeDocumentation.combine(
        CodeDocumentation.text("Commands should only be registered during the "),
        CodeDocumentation.classReference(VelocityClasses.PROXY_INITIALIZE_EVENT),
        CodeDocumentation.text(".")
      ),
      CodeDocumentation.text("The example below shows an example of how to do this. For more information,"),
      CodeDocumentation.combine(CodeDocumentation.text("refer to "), CodeDocumentation.url("The Velocity Command API docs", "https://docs.papermc.io/velocity/dev/command-api/#registering-a-command")),

      CodeDocumentation.blank(),

      CodeDocumentation.codeBlock("""
        @Subscribe
        void onProxyInitialize(final ProxyInitializeEvent event) {
          %s.register(this.proxy, this);
        }""".formatted(selfType.name()))
    ));
  }
}
