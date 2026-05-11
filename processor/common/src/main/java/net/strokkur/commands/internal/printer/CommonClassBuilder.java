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
import net.strokkur.commands.internal.abstraction.SourceConstructor;
import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.abstraction.SourceTypeAnnotation;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.codegen.CodeAnnotation;
import net.strokkur.commands.internal.codegen.CodeClass;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.CodeMethod;
import net.strokkur.commands.internal.codegen.CodePackage;
import net.strokkur.commands.internal.codegen.CodeStatement;
import net.strokkur.commands.internal.codegen.CodeType;
import net.strokkur.commands.internal.codegen.Modifiers;
import net.strokkur.commands.internal.codegen.adapter.CodeTypeAdapter;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.codegen.builder.ClassBuilder;
import net.strokkur.commands.internal.codegen.builder.MethodBuilder;
import net.strokkur.commands.internal.codegen.builder.MethodInvocationBuilder;
import net.strokkur.commands.internal.codegen.javadoc.CodeJavadoc;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.printer.source.AbstractSourcePrintingVisitor;
import net.strokkur.commands.internal.printer.source.ImportGatheringVisitor;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.internal.util.ConvertableTo;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class CommonClassBuilder<C extends CommandInformation> {
  private final CommandNode rootNode;
  protected final C commandInformation;
  private final CommonBrigadierStatementBuilder statementBuilder;
  private final BiFunction<CodePackage, Set<CodeType.ClassType>, AbstractSourcePrintingVisitor> sourceVisitor;

  protected final CodeType.ClassType sourceType;
  protected final CodeType.ClassType selfType;

  public CommonClassBuilder(
      CommandNode rootNode,
      C commandInformation,
      CommonBrigadierStatementBuilder statementBuilder,
      BiFunction<CodePackage, Set<CodeType.ClassType>, AbstractSourcePrintingVisitor> sourceVisitor
  ) {
    this.rootNode = rootNode;
    this.commandInformation = commandInformation;
    this.statementBuilder = statementBuilder;
    this.sourceVisitor = sourceVisitor;

    this.sourceType = CodeType.ofClass(commandInformation.sourceClass().getFullyQualifiedName());
    this.selfType = CodeType.ofClass(sourceType.fullyQualifiedName() + "Brigadier");
  }

  /// Converts the passed root command node into a proper Java file.
  public final String getAsString() {
    final CodeClass brigadierClass = createClass();

    final StringBuilder out = new StringBuilder();
    out.append("package ").append(selfType.codePackage().path()).append(";\n\n");

    final Set<CodeType.ClassType> imports = gatherAndAppendImports(out, brigadierClass);
    out.append("\n");

    final AbstractSourcePrintingVisitor visitor = sourceVisitor.apply(brigadierClass.codePackage(), imports);
    out.append(brigadierClass.accept(visitor));
    return out.toString();
  }

  /// Creates the actual class, which will be printed to a file.
  private CodeClass createClass() {
    // Create skeletons for create and register methods for use in Javadocs.
    final MethodBuilder createMethod = getCreateMethodBuilder();
    final MethodBuilder registerMethod = getRegisterMethodBuilder();

    applyCreateMethodJavadoc(createMethod, registerMethod);
    applyRegisterMethodJavadoc(registerMethod, createMethod);

    // Start building up the actual class
    final ClassBuilder classBuilder = Builders.classBuilder(selfType.fullyQualifiedName());
    classBuilder.setJavadoc(getClassJavadoc(createMethod, registerMethod));
    classBuilder.setModifiers(Modifiers.PUBLIC, Modifiers.FINAL);
    classBuilder.addAnnotations(CodeAnnotation.NULL_MARKED);

    populateStaticFields(classBuilder);

    // Run the brigadier tree builder so we can use the statements
    statementBuilder.reset();
    final AsExpression treeExpr = statementBuilder.build(rootNode, CodeExpression.variable("NAME"));

    final List<PrintedAccessPath> required = statementBuilder.requiredPaths.stream()
        .map(PrintedAccessPath::requiredParent)
        .distinct()
        .sorted(Comparator.comparing(PrintedAccessPath::name))
        .toList();
    if (commandInformation.useInjection()) {
      // Use injection for the required fields
      required.forEach(path -> {
        classBuilder.addField(Builders.field(path.name(), path.access().getLast().getAsCodeType())
            .setModifiers(Modifiers.PRIVATE)
            .addAnnotation(CodeAnnotation.INJECT)
        );
      });
    } else {
      // We are not using injection, so instead create the instances inside the create method
      required.forEach(path -> {
        createMethod.addMethodStatements(CodeStatement.variableDeclarationFinal(
            path.access().getLast(),
            path.name(),
            createInstanceConstructor((CodeType.ClassType) path.access().getLast().getAsCodeType())
        ));
      });
      if (!required.isEmpty()) {
        createMethod.addMethodStatements(CodeStatement.blank());
      }
    }

    createMethod.addMethodStatements(CodeStatement.returnStatement(treeExpr));

    // Add the methods to the class
    classBuilder.addMethod(registerMethod);
    classBuilder.addMethod(createMethod);

    // If the class is not injectable, the ctor should be private
    if (!commandInformation.useInjection()) {
      classBuilder.addMethod(Builders.method(selfType.codeClass())
          .setJavadoc(CodeJavadoc.combineLines(
              CodeJavadoc.text("The constructor is not accessible. There is no need for an instance"),
              CodeJavadoc.text("to be created, as no state is stored and all methods are static."),
              CodeJavadoc.blank(),
              CodeJavadoc.throwsMeta(Classes.ILLEGAL_ACCESS_EXCEPTION, "always")
          ))
          .setThrowsExceptions(Classes.ILLEGAL_ACCESS_EXCEPTION)
          .setMethodStatements(CodeStatement.throwStatement(
              Builders.ctorInvocation(Classes.ILLEGAL_ACCESS_EXCEPTION).addParameter(CodeExpression.string(
                  "This class cannot be instantiated."
              ))
          ))
      );
    }

    return classBuilder.build();
  }

  private void addSourceConstructorParameters(MethodInvocationBuilder builder) {
    if (commandInformation.useInjection()) {
      // Don't add constructor parameters
      return;
    }

    if (commandInformation.constructor() instanceof SourceConstructor sourceCtor) {
      for (SourceParameter parameter : sourceCtor.getParameters()) {
        builder.addParameter(CodeExpression.variable(parameter.getName()));
      }
    }
  }

  protected AsExpression createInstanceConstructor(CodeType.ClassType classType) {
    final MethodInvocationBuilder ctor = Builders.ctorInvocation(classType);
    if (sourceType.equals(classType)) {
      addSourceConstructorParameters(ctor);
    }
    return ctor;
  }

  /// The transmutation logic for the top-level constructor call, intended
  /// for using existing instances (i.e., a Server instance) multiple times
  /// to save on duplicate parameters in the create/register methods.
  protected AsExpression transmuteConstructorParameter(SourceVariable parameter) {
    return CodeExpression.variable(parameter.getName());
  }

  protected void addConstructorParametersTo(MethodBuilder builder, Predicate<SourceParameter> filter) {
    if (!commandInformation.useInjection() && commandInformation.constructor() instanceof SourceConstructor ctor) {
      for (SourceTypeAnnotation typeAnnotation : ctor.getTypeAnnotations()) {
        builder.addGeneric(CodeType.generic(typeAnnotation.getName(), typeAnnotation.getDefinitionString()));
      }

      for (SourceParameter parameter : ctor.getParameters()) {
        if (filter.test(parameter)) {
          builder.addParameter(CodeTypeAdapter.from(parameter.getType()), parameter.getName());
        }
      }
    }
  }

  /// Creates the builder for the create method.
  ///
  /// @apiNote this method should always be overridden. Overriders should implement the create method logic now.
  @MustBeInvokedByOverriders
  protected MethodBuilder getCreateMethodBuilder() {
    final MethodBuilder builder = Builders.method("create");
    builder.setModifiers(Modifiers.PUBLIC);
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
    final MethodBuilder builder = Builders.method("register");
    builder.setModifiers(Modifiers.PUBLIC);
    if (!commandInformation.useInjection()) {
      builder.addModifiers(Modifiers.STATIC);
    }
    return builder;
  }

  /// Populates the class with static fields, intended to hold information supplied from the
  /// source class, such as the command name, command description, and aliases.
  @MustBeInvokedByOverriders
  protected void populateStaticFields(ClassBuilder builder) {
    builder.addField(Builders.field("NAME", CodeType.STRING)
        .setModifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
        .setInitialiser(CodeExpression.string(rootNode.argument().argumentName())) // The name of the command
    );
  }

  /// Sets the Javadoc for the create method.
  protected void applyCreateMethodJavadoc(MethodBuilder createMethod, ConvertableTo<CodeMethod> registerMethod) {
    createMethod.setJavadoc(CodeJavadoc.combineLines(
        CodeJavadoc.text("A method for creating a Brigadier command node which denotes the declared command"),
        CodeJavadoc.combine(
            CodeJavadoc.text("in "),
            CodeJavadoc.classReference(sourceType.codeClass()),
            CodeJavadoc.text(". You can either retrieve the unregistered node with this method")),
        CodeJavadoc.combine(
            CodeJavadoc.text("or register it directly with "),
            CodeJavadoc.methodReference(registerMethod, true),
            CodeJavadoc.text("."))
    ));
  }

  /// Sets the Javadoc for the register method. Not directly implemented due to platform-dependent differences
  /// in command registration.
  protected abstract void applyRegisterMethodJavadoc(MethodBuilder registerMethod, ConvertableTo<CodeMethod> createMethod);

  /// Gets the Javadoc for the class file, cannot currently be overriden.
  private CodeJavadoc getClassJavadoc(ConvertableTo<CodeMethod> createMethod, ConvertableTo<CodeMethod> registerMethod) {
    return CodeJavadoc.combineLines(
        CodeJavadoc.text("A class holding the Brigadier source tree generated from"),
        CodeJavadoc.combine(
            CodeJavadoc.classReference(CodeClass.simple(commandInformation.sourceClass().getFullyQualifiedName())),
            CodeJavadoc.text(" using "),
            CodeJavadoc.url("StrokkCommands", "https://commands.strokkur.net")
        ),
        CodeJavadoc.blank(),
        CodeJavadoc.author("Strokkur24 - StrokkCommands"),
        CodeJavadoc.version(BuildConstants.VERSION),
        CodeJavadoc.see(createMethod, "creating the command", true),
        CodeJavadoc.see(registerMethod, "registering the command", true)
    );
  }

  /// This method constructs an import-gathering visitor, gathers all imports from the class,
  /// and finally splits them by Java and non-Java imports, sorts them, and appends them to the string builder.
  /// This mimics IntelliJ IDEA's own import format behavior.
  ///
  /// @return all collected imports
  private Set<CodeType.ClassType> gatherAndAppendImports(StringBuilder builder, CodeClass brigadierClass) {
    final ImportGatheringVisitor importVisitor = new ImportGatheringVisitor();
    final Set<CodeType.ClassType> imports = importVisitor.collectFilteredImports(brigadierClass);

    final Map<Boolean, List<CodeType.ClassType>> split = imports.stream()
        .collect(Collectors.partitioningBy(type -> type.codePackage().path().startsWith("java")));

    split.get(false).stream().sorted()
        .forEach(type -> builder.append("import ").append(type.fullyQualifiedName()).append(";\n"));

    if (!split.get(false).isEmpty() && !split.get(true).isEmpty()) {
      builder.append("\n");
    }

    split.get(true).stream().sorted()
        .forEach(type -> builder.append("import ").append(type.fullyQualifiedName()).append(";\n"));

    return imports;
  }
}
