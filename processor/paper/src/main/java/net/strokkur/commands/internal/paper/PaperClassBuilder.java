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
package net.strokkur.commands.internal.paper;

import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.paper.util.PaperClasses;
import net.strokkur.commands.internal.paper.util.PaperCommandInformation;
import net.strokkur.commands.internal.printer.CommonClassBuilder;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.jap.code.classmodel.CodeField;
import net.strokkur.jap.code.classmodel.CodeMethod;
import net.strokkur.jap.code.classmodel.CodeParameterDefinition;
import net.strokkur.jap.code.classmodel.builder.ClassBuilder;
import net.strokkur.jap.code.classmodel.builder.MethodBuilder;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToMethod;
import net.strokkur.jap.code.documentation.CodeDocumentation;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.MethodInvocationBuilder;
import net.strokkur.jap.code.type.preset.JSpecifyTypes;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.code.util.Modifiers;
import net.strokkur.jap.source.classmodel.SourceConstructor;
import net.strokkur.jap.source.classmodel.SourceMethodParameter;

import static net.strokkur.jap.code.documentation.CodeDocumentation.codeBlock;
import static net.strokkur.jap.code.documentation.CodeDocumentation.combine;
import static net.strokkur.jap.code.documentation.CodeDocumentation.header;
import static net.strokkur.jap.code.documentation.CodeDocumentation.linebreak;
import static net.strokkur.jap.code.documentation.CodeDocumentation.see;
import static net.strokkur.jap.code.documentation.CodeDocumentation.text;

final class PaperClassBuilder extends CommonClassBuilder<PaperCommandInformation> {
  PaperClassBuilder(CommandNode rootNode, PaperCommandInformation commandInformation) {
    super(rootNode, commandInformation);
  }

  @Override
  protected void populateStaticFields(ClassBuilder builder) {
    super.populateStaticFields(builder);
    final ConvertToExpression description = commandInformation.description() == null ?
      Expressions.nullExpr() :
      Expressions.string(commandInformation.description());

    final MethodInvocationBuilder aliases = JavaTypes.LIST.chainMethod("of");
    if (commandInformation.aliases() != null) {
      for (String alias : commandInformation.aliases()) {
        aliases.addParameters(Expressions.string(alias));
      }
    }

    builder.addFields(
      CodeField.builder(JavaTypes.STRING, "DESCRIPTION")
        .addModifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
        .addAnnotations(JSpecifyTypes.NULLABLE)
        .setInitializer(description),
      CodeField.builder(JavaTypes.LIST.typed(JavaTypes.STRING), "ALIASES")
        .addModifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
        .setInitializer(aliases)
    );
  }

  @Override
  protected MethodBuilder getCreateMethodBuilder() {
    return super.getCreateMethodBuilder()
      .setReturnType(Classes.LITERAL_COMMAND_NODE.typed(PaperClasses.COMMAND_SOURCE_STACK));
  }

  @Override
  protected MethodBuilder getRegisterMethodBuilder() {
    final MethodInvocationBuilder createInvocation = new MethodInvocationBuilder()
      .setName("create");

    if (!commandInformation.useInjection()) {
      if (commandInformation.constructor() instanceof SourceConstructor sourceCtor) {
        for (SourceMethodParameter parameter : sourceCtor.parameters()) {
          createInvocation.addParameters(Expressions.variable(parameter.name()));
        }
      }
    }

    final MethodInvocationBuilder registerExpr = Expressions.variable("commands")
      .chainMethod("register", createInvocation);
    if (commandInformation.description() != null) {
      registerExpr.addParameters(Expressions.variable("DESCRIPTION"));
    }
    if (commandInformation.aliases() != null && commandInformation.aliases().length > 0) {
      registerExpr.addParameters(Expressions.variable("ALIASES"));
    }

    final MethodBuilder builder = super.getRegisterMethodBuilder()
      .addParameters(CodeParameterDefinition.of(PaperClasses.COMMANDS, "commands"))
      .setCodeBlock(registerExpr);

    addConstructorParametersTo(builder, f -> true);
    return builder;
  }

  @Override
  protected void applyRegisterMethodJavadoc(MethodBuilder registerMethod, ConvertToMethod createMethod) {
    final ConvertToMethod bootstrapMethod = CodeMethod.builder("bootstrap")
      .addParameter(PaperClasses.BOOTSTRAP_CONTEXT, "context");

    registerMethod.setDocumentation(CodeDocumentation.combineLines(
      text("Shortcut for registering the command node returned from"),
      combine(see(createMethod, null), text(". This method uses the provided aliases")),
      text("and description from the original source file."),
      linebreak(),
      header("Registering the command", 3),
      linebreak(),
      text("This method can safely be called either in your plugin bootstrapper's"),
      combine(see(bootstrapMethod, null, PaperClasses.PLUGIN_BOOTSTRAP), text(", your main")),
      combine(
        text("class' "),
        see(CodeMethod.builder("onLoad"), null, PaperClasses.JAVA_PLUGIN),
        text(" or "),
        see(CodeMethod.builder("onEnable"), null, PaperClasses.JAVA_PLUGIN)),
      text("method."),
      linebreak(),
      codeBlock("""
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            final Commands commands = event.registrar();
            %s.register(commands);
        }""".formatted(selfType.name()))
    ));
  }

  @Override
  protected void applyCreateMethodJavadoc(MethodBuilder createMethod, ConvertToMethod registerMethod) {
    super.applyCreateMethodJavadoc(createMethod, registerMethod);
  }
}
