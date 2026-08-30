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
package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.BuildConstants;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.registrable.ExecutorWrapperProvider;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.prototype.PrototypeNodeBuilder;
import net.strokkur.commands.internal.prototype.PrototypeRoot;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.jap.code.classmodel.*;
import net.strokkur.jap.code.classmodel.builder.ClassBuilder;
import net.strokkur.jap.code.classmodel.builder.MethodBuilder;
import net.strokkur.jap.code.convert.ConvertToClassType;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToMethod;
import net.strokkur.jap.code.convert.ConvertToStatement;
import net.strokkur.jap.code.documentation.CodeDocumentation;
import net.strokkur.jap.code.expression.CodeExpression;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.ConstructorInvocationBuilder;
import net.strokkur.jap.code.expression.builder.MethodLikeInvocationBuilder;
import net.strokkur.jap.code.statement.Statements;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.type.CodeTypes;
import net.strokkur.jap.code.type.preset.JSpecifyTypes;
import net.strokkur.jap.code.type.preset.JakartaInjectTypes;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.code.util.Modifiers;
import net.strokkur.jap.code.util.StyleConfig;
import net.strokkur.jap.source.classmodel.SourceConstructor;
import net.strokkur.jap.source.classmodel.SourceMethodParameter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class CommonClassBuilder<C extends CommandInformation> implements ForwardingMessagerWrapper {
  private final CommandNode rootNode;
  protected final C commandInformation;

  protected final CodeClassType sourceType;
  protected final CodeClassType selfType;

  public CommonClassBuilder(CommandNode rootNode, C commandInformation) {
    this.rootNode = rootNode;
    this.commandInformation = commandInformation;
    this.sourceType = commandInformation.sourceClass().classType();
    this.selfType = CodeTypes.ofClass(sourceType.fullyQualifiedName() + "Brigadier");
  }

  /// Creates the actual class, which will be printed to a file.
  public CodeClass createClass() {
    // Create skeletons for create and register methods for use in Javadocs.
    final MethodBuilder createMethod = getCreateMethodBuilder();
    final MethodBuilder createMethodWithName = getCreateMethodBuilderWithName();
    final MethodBuilder registerMethod = getRegisterMethodBuilder();

    final List<ConvertToStatement> createMethodStatements = new ArrayList<>();

    applyCreateMethodJavadoc(createMethod, registerMethod);
    applyCreateMethodJavadoc(createMethodWithName, registerMethod);
    applyRegisterMethodJavadoc(registerMethod, createMethod);

    // Start building up the actual class
    final ClassBuilder classBuilder = CodeClass.builder(selfType);
    classBuilder.setDocumentation(getClassJavadoc(createMethod, registerMethod));
    classBuilder.setDocumentation(getClassJavadoc(createMethodWithName, registerMethod));
    classBuilder.addModifiers(Modifiers.PUBLIC, Modifiers.FINAL);
    classBuilder.addAnnotations(JSpecifyTypes.NULL_MARKED);

    populateStaticFields(classBuilder);

    final PrototypeNodeBuilder nodeBuilder = PrototypeNodeBuilder.create();
    final PrototypeRoot prototype = nodeBuilder.createRoot(rootNode);
    prototype.preProcess();
    final ConvertToExpression treeExpr = prototype.toExpression()
      .chainMethod("build", StyleConfig.NEWLINE);

    for (String warning : nodeBuilder.warnings()) {
      warn(warning);
    }
    for (String warning : nodeBuilder.errors()) {
      error(warning);
    }

    final List<PrintedAccessPath> required = nodeBuilder.requiredPaths().stream()
      .flatMap(p -> p.allRequired().stream())
      .distinct()
      .sorted(Comparator.comparing(PrintedAccessPath::name))
      .toList();
    if (commandInformation.useInjection()) {
      // Use injection for the required fields
      required.forEach(path -> {
        classBuilder.addFields(CodeField.builder(path.access().getLast(), path.name())
          .addModifiers(Modifiers.PRIVATE)
          .addAnnotations(JakartaInjectTypes.INJECT)
        );
      });
    } else {
      // We are not using injection, so instead create the instances inside the create method
      required.forEach(path -> {
        createMethodStatements.add(Statements.variableDeclarationFinal(
          path.access().getLast(),
          path.name(),
          path.getInitializer(commandInformation)
        ));
      });
      if (!required.isEmpty()) {
        debug("Required access paths:%s", required.stream()
          .map(p -> "\n- " + p.access().stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "))
          )
          .collect(Collectors.joining())
        );
        createMethodStatements.add(Statements.blank());
      }
    }

    createMethodStatements.add(Statements.returnStmt(treeExpr));

    createMethod.setCode(
      Statements.returnStmt(
        Expressions.methodInvocation(
          "create"
        ).addParameters(Expressions.variable("NAME"))
      )
    );

    createMethodWithName.setCode(createMethodStatements.toArray(ConvertToStatement[]::new));

    // Add the methods to the class
    classBuilder.addMethods(registerMethod, createMethod, createMethodWithName);

    // If the class is not injectable, the ctor should be private
    if (!commandInformation.useInjection()) {
      classBuilder.addConstructor(builder -> builder
        .setDocumentation(CodeDocumentation.combineLines(
          CodeDocumentation.text("The constructor is not accessible. There is no need for an instance"),
          CodeDocumentation.text("to be created, as no state is stored and all methods are static."),
          CodeDocumentation.blank(),
          CodeDocumentation.throwsMeta(JavaTypes.ILLEGAL_ACCESS_EXCEPTION, "always")
        ))
        .addModifiers(Modifiers.PRIVATE)
        .addThrowsExceptions(JavaTypes.ILLEGAL_ACCESS_EXCEPTION)
        .setCodeBlock(JavaTypes.ILLEGAL_ACCESS_EXCEPTION
          .ctor(Expressions.string("This class cannot be instantiated."))
          .throwStmt()
        )
      );
    }

    final boolean genHelperMethod = rootNode.stream().anyMatch(
      node -> node.getAttribute(AttributeKey.EXECUTOR_WRAPPER) instanceof ExecutorWrapperProvider provider
        && provider.wrapperType().withMethod());
    if (genHelperMethod) {
      classBuilder.addMethods(createReflectionHelper());
    }

    return classBuilder.build();
  }

  private void addSourceConstructorParameters(MethodLikeInvocationBuilder<?> builder) {
    if (commandInformation.useInjection()) {
      // Don't add constructor parameters
      return;
    }

    if (commandInformation.constructor() instanceof SourceConstructor sourceCtor) {
      builder.addParameters(sourceCtor.parameters().stream()
        .map(p -> Expressions.variable(p.name()))
        .toArray(CodeExpression[]::new)
      );
    }
  }

  protected ConvertToExpression createInstanceConstructor(ConvertToClassType classType) {
    final ConstructorInvocationBuilder ctor = classType.ctor();
    if (sourceType.equals(classType)) {
      addSourceConstructorParameters(ctor);
    }
    return ctor;
  }

  /// The transmutation logic for the top-level constructor call, intended
  /// for using existing instances (i.e., a Server instance) multiple times
  /// to save on duplicate parameters in the create/register methods.
  protected ConvertToExpression transmuteConstructorParameter(SourceMethodParameter parameter) {
    return Expressions.variable(parameter.name());
  }

  protected void addConstructorParametersTo(MethodBuilder builder, Predicate<SourceMethodParameter> filter) {
    if (!commandInformation.useInjection() && commandInformation.constructor() instanceof SourceConstructor ctor) {
      // for (SourceTypeAnnotation typeAnnotation : ctor.getTypeAnnotations()) {
      //   builder.addGeneric(CodeType.generic(typeAnnotation.getName(), typeAnnotation.getDefinitionString()));
      // }

      for (SourceMethodParameter parameter : ctor.parameters()) {
        if (filter.test(parameter)) {
          builder.addParameter(parameter.type(), parameter.name());
        }
      }
    }
  }

  /// Creates the builder for the create method.
  ///
  /// @apiNote this method should always be overridden. Overriders should implement the create method logic now.
  @MustBeInvokedByOverriders
  protected MethodBuilder getCreateMethodBuilder() {
    final MethodBuilder builder = CodeMethod.builder("create");
    builder.addModifiers(Modifiers.PUBLIC);
    if (!commandInformation.useInjection()) {
      builder.addModifiers(Modifiers.STATIC);
    }

    // Propagate constructor parameters
    addConstructorParametersTo(builder, f -> true);

    return builder;
  }

  /// Creates the builder for the create method.
  ///
  /// @apiNote this method should always be overridden. Overriders should implement the create method logic now.
  @MustBeInvokedByOverriders
  protected MethodBuilder getCreateMethodBuilderWithName() {
    final MethodBuilder builder = CodeMethod.builder("create");
    builder.addModifiers(Modifiers.PUBLIC);
    builder.addParameter(CodeTypes.ofJavaClass(String.class), "commandName");
    if (!commandInformation.useInjection()) {
      builder.addModifiers(Modifiers.STATIC);
    }

    // Propagate constructor parameters
    addConstructorParametersTo(builder, f -> true);

    return builder;
  }


  /// Creates the builder for the register method.
  ///
  /// @apiNote this method should always be overridden. Overrides should **not** implement any logic at this point.
  @MustBeInvokedByOverriders
  protected MethodBuilder getRegisterMethodBuilder() {
    final MethodBuilder builder = CodeMethod.builder("register");
    builder.addModifiers(Modifiers.PUBLIC);
    if (!commandInformation.useInjection()) {
      builder.addModifiers(Modifiers.STATIC);
    }
    return builder;
  }

  /// Populates the class with static fields, intended to hold information supplied from the
  /// source class, such as the command name, command description, and aliases.
  @MustBeInvokedByOverriders
  protected void populateStaticFields(ClassBuilder builder) {
    builder.addFields(CodeField.builder(JavaTypes.STRING, "NAME")
      .addModifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
      .setInitializer(Expressions.string(commandInformation.name()))
    );
  }

  /// Sets the Javadoc for the create method.
  protected void applyCreateMethodJavadoc(MethodBuilder createMethod, ConvertToMethod registerMethod) {
    createMethod.setDocumentation(CodeDocumentation.combineLines(
      CodeDocumentation.text("A method for creating a Brigadier command node which denotes the declared command"),
      CodeDocumentation.combine(
        CodeDocumentation.text("in "),
        CodeDocumentation.classReference(sourceType),
        CodeDocumentation.text(". You can either retrieve the unregistered node with this method")),
      CodeDocumentation.combine(
        CodeDocumentation.text("or register it directly with "),
        CodeDocumentation.methodReference(registerMethod),
        CodeDocumentation.text("."))
    ));
  }

  /// Sets the Javadoc for the register method. Not directly implemented due to platform-dependent differences
  /// in command registration.
  protected abstract void applyRegisterMethodJavadoc(MethodBuilder registerMethod, ConvertToMethod createMethod);

  protected CodeMethod createReflectionHelper() {
    return CodeMethod.builder("getMethodReflectively")
      .addModifiers(Modifiers.PRIVATE, Modifiers.STATIC)
      .setReturnType(CodeTypes.ofJavaClass(Method.class))
      .addParameter(CodeTypes.ofJavaClass(Class.class).typed(CodeTypes.genericWildcard()), "clazz")
      .addParameter(JavaTypes.STRING, "name")
      .addParameters(CodeParameterDefinition.ofVarargs(
        CodeTypes.ofJavaClass(Class.class).typed(CodeTypes.genericWildcard()),
        "parameters"
      ))
      .setCode(
        Statements.tryStmt(
          CodeBlock.of(
            Statements.returnStmt(Expressions.variable("clazz").chainMethod("getDeclaredMethod",
              Expressions.variable("name"),
              Expressions.variable("parameters")
            ))
          ),
          CodeTypes.ofJavaClass(ReflectiveOperationException.class),
          "ex",
          CodeBlock.of(
            JavaTypes.RUNTIME_EXCEPTION.ctor(Expressions.variable("ex")).throwStmt()
          )
        )
      )
      .toMethod();
  }

  /// Gets the Javadoc for the class file, cannot currently be overridden.
  private CodeDocumentation getClassJavadoc(ConvertToMethod createMethod, ConvertToMethod registerMethod) {
    return CodeDocumentation.combineLines(
      CodeDocumentation.text("A class holding the Brigadier source tree generated from"),
      CodeDocumentation.combine(
        CodeDocumentation.classReference(commandInformation.sourceClass()),
        CodeDocumentation.text(" using "),
        CodeDocumentation.url("StrokkCommands", "https://commands.strokkur.net")
      ),
      CodeDocumentation.blank(),
      CodeDocumentation.author("Strokkur24 - StrokkCommands"),
      CodeDocumentation.version(BuildConstants.VERSION),
      CodeDocumentation.see(createMethod, "creating the command"),
      CodeDocumentation.see(registerMethod, "registering the command")
    );
  }
}
