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
package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.Command;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Literal;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.UnsetExecutorWrapper;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.exceptions.ConversionException;
import net.strokkur.commands.internal.exceptions.IllegalCommandClassTypeException;
import net.strokkur.commands.internal.exceptions.IllegalSubcommandFieldType;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.Attributable;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.CommandParameter;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.executable.UnparsedCommandParameter;
import net.strokkur.commands.internal.intermediate.record.RecordArguments;
import net.strokkur.commands.internal.intermediate.registrable.CombinedRequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.ExecutorWrapperRegistry;
import net.strokkur.commands.internal.intermediate.registrable.RegistrableRegistry;
import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.RequirementRegistry;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionsRegistry;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.jap.source.annotation.AnnotationsHolder;
import net.strokkur.jap.source.annotation.SourceAnnotation;
import net.strokkur.jap.source.classmodel.SourceAnnotationInterface;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceConstructor;
import net.strokkur.jap.source.classmodel.SourceField;
import net.strokkur.jap.source.classmodel.SourceInterface;
import net.strokkur.jap.source.classmodel.SourceMethod;
import net.strokkur.jap.source.classmodel.SourceParameterLike;
import net.strokkur.jap.source.classmodel.SourceRecord;
import net.strokkur.jap.source.visitor.SourceVisitor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandParsingSourceVisitor implements SourceVisitor<CommandNode, Void>, ForwardingMessagerWrapper {
  private static final CommandParsingSourceVisitor INSTANCE = new CommandParsingSourceVisitor();

  public static CommandParsingSourceVisitor get() {
    return INSTANCE;
  }

  //
  // Methods unique to this visitor for better DX when parsing commands.
  //

  /// Handles [Command]-annotated classes instead of the standard [Subcommand].
  public CommandNode visitCommandClass(SourceClassLike classLike, Void unused) {
    final CommandNode node = switch (classLike) {
      case SourceClass sourceClass -> visitClass(sourceClass, unused, Command.class, Command::value);
      case SourceRecord record -> visitRecord(record, unused, Command.class, Command::value);
      default -> throw new IllegalCommandClassTypeException(classLike);
    };

    // A class annotated with @Command will ALWAYS result in a root empty node,
    // followed by a single child, named the command name (ofc it may have a longer path too).
    // For this reason, and to simplify further parsing down the line, move all attributes
    // to the first child and return that first child instead.
    final CommandNode child = node.children().getFirst();
    node.transferAllAttributes(child);
    return child;
  }

  //
  // Normal visitor pattern methods.
  //

  @Override
  public CommandNode visitClass(SourceClass sourceClass, Void unused) {
    return visitClass(sourceClass, unused, Subcommand.class, Subcommand::value);
  }

  @Override
  public CommandNode visitRecord(SourceRecord sourceRecord, Void unused) {
    return visitRecord(sourceRecord, unused, Subcommand.class, Subcommand::value);
  }

  @Override
  public CommandNode visitMethod(SourceMethod sourceMethod, Void unused) {
    final List<CommandParameter> arguments = sourceMethod.parameters().stream()
      .map(this::parseParameter)
      .toList();
    final List<CommandArgument> commandArguments = arguments.stream()
      .filter(CommandArgument.class::isInstance)
      .map(CommandArgument.class::cast)
      .toList();

    final CommandNode rootNode = CommandNode.createEmpty();
    final Executable executable = new Executable(sourceMethod.enclosed(), sourceMethod, arguments);

    applyExecutesLogic(
      sourceMethod, rootNode,
      Executes.class, Executes::value,
      arguments, commandArguments,
      new Executable(executable), AttributeKey.EXECUTABLE
    );
    if (sourceMethod.hasAnnotationInherited(DefaultExecutes.class)) {
      applyExecutesLogic(
        sourceMethod, rootNode,
        DefaultExecutes.class, DefaultExecutes::value,
        arguments, commandArguments,
        new DefaultExecutable(executable), AttributeKey.DEFAULT_EXECUTABLE
      );
    }

    // Apply attributes
    applyExecutorTransform(sourceMethod, rootNode);
    PlatformUtils.get().populateNode(sourceMethod, rootNode);
    applyRequirements(sourceMethod, rootNode);

    return rootNode;
  }

  @Override
  public CommandNode visitField(SourceField sourceField, Void unused) {
    final CommandNode nestedNode = switch (sourceField.type()) {
      case SourceClass sourceClass -> sourceClass.accept(this, unused);
      case SourceRecord record -> record.accept(this, unused);
      default -> throw new IllegalSubcommandFieldType(sourceField.type());
    };

    final CommandNode rootNode = CommandNode.createEmpty();
    forEachPathAnnotation(
      sourceField, rootNode,
      Subcommand.class, Subcommand::value,
      node -> node.addChild(nestedNode)
    );

    // Apply attribute modifiers
    rootNode.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(sourceField));
    applyExecutorTransform(sourceField, rootNode);
    PlatformUtils.get().populateNode(sourceField, rootNode);
    applyRequirements(sourceField, rootNode);

    return nestedNode;
  }

  //
  // Util/common methods.
  //

  private <A extends Annotation> CommandNode visitClass(
    SourceClass sourceClass, Void unused,
    Class<A> annotationClass, Function<A, String> toPath
  ) {
    // Parse nested elements first, so that the same CommandNode instances can be reused inside the
    // final node consumers.
    final List<CommandNode> nestedNodes = new ArrayList<>();
    nestedNodes.addAll(sourceClass.methods().stream()
      .filter(method -> method.hasAnnotationInherited(Executes.class) || method.hasAnnotationInherited(DefaultExecutes.class))
      .map(method -> method.accept(this, unused))
      .toList());
    nestedNodes.addAll(sourceClass.fields().stream()
      .filter(field -> field.hasAnnotationInherited(Subcommand.class))
      .map(field -> field.accept(this, unused))
      .toList());
    nestedNodes.addAll(sourceClass.nestedClasses().stream()
      .filter(nested -> nested.hasAnnotationInherited(Subcommand.class))
      .map(nested -> nested.accept(this, unused))
      .toList());

    final CommandNode rootNode = CommandNode.createEmpty();
    forEachPathAnnotation(
      sourceClass, rootNode,
      annotationClass, toPath,
      node -> nestedNodes.forEach(node::addChild)
    );

    // Apply some attributes to the root node before returning it.
    rootNode.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(sourceClass));
    applyExecutorTransform(sourceClass, rootNode);
    PlatformUtils.get().populateNode(sourceClass, rootNode);
    applyRequirements(sourceClass, rootNode);

    return rootNode;
  }

  private <A extends Annotation> CommandNode visitRecord(
    SourceRecord record, Void unused,
    Class<A> annotationClass, Function<A, String> toPath
  ) {
    // Parse nested elements first, so that the same CommandNode instances can be reused inside the
    // final node consumers.
    final List<CommandNode> nestedNodes = new ArrayList<>();
    nestedNodes.addAll(record.methods().stream()
      .filter(method -> method.hasAnnotationInherited(Executes.class) || method.hasAnnotationInherited(DefaultExecutes.class))
      .map(method -> method.accept(this, unused))
      .toList());
    nestedNodes.addAll(record.nestedClasses().stream()
      .filter(nested -> nested.hasAnnotationInherited(Subcommand.class))
      .map(nested -> nested.accept(this, unused))
      .toList());

    final CommandNode rootNode = CommandNode.createEmpty();
    final List<CommandParameter> parsedComponents = record.components().stream()
      .map(this::parseParameter)
      .toList();

    final CommandNode postArgumentsNode = rootNode.addArguments(parsedComponents.stream()
      .filter(CommandArgument.class::isInstance)
      .map(CommandArgument.class::cast)
      .toList());

    forEachPathAnnotation(
      record, postArgumentsNode,
      annotationClass, toPath,
      node -> nestedNodes.forEach(node::addChild)
    );

    // Apply some attributes to the root node before returning it.
    rootNode.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(record));
    rootNode.setAttribute(AttributeKey.RECORD_ARGUMENTS, RecordArguments.of(record, parsedComponents));
    applyExecutorTransform(record, rootNode);
    PlatformUtils.get().populateNode(record, rootNode);
    applyRequirements(record, rootNode);

    return rootNode;
  }

  private <A extends Annotation, E extends Executable> void applyExecutesLogic(
    SourceMethod sourceMethod, CommandNode root,
    Class<A> annotationClass, Function<A, String> toPath,
    List<CommandParameter> arguments, List<CommandArgument> commandArguments,
    E executable, AttributeKey<E> key
  ) {
    forEachPathAnnotation(
      sourceMethod, root,
      annotationClass, toPath,
      node -> {
        final CommandNode endNode = node.addArguments(commandArguments);
        endNode.setAttribute(key, executable);
        PlatformUtils.get().populateExecutesNode(executable, endNode, arguments);
      }
    );
  }

  private <A extends Annotation> void forEachPathAnnotation(
    AnnotationsHolder holder, CommandNode root,
    Class<A> annotationClass, Function<A, String> toPath,
    Consumer<CommandNode> endNodeConsumer
  ) {
    if (!holder.hasAnnotationInherited(annotationClass)) {
      endNodeConsumer.accept(root);
      return;
    }

    holder.annotationsInherited(annotationClass).stream()
      .map(a -> {
        final A anno = a.value(annotationClass);
        return toPath.apply(anno);
      })
      .distinct()
      .map(path -> path.isBlank() ?
        List.<CommandArgument>of() :
        Arrays.stream(path.strip().split(" "))
          .map(LiteralCommandArgument::new)
          .toList())
      .forEach(args -> {
        final CommandNode endNode = args.isEmpty() ? root : root.addArguments(args);
        endNodeConsumer.accept(endNode);
      });
  }

  private void applyRequirements(AnnotationsHolder holder, CommandNode node) {
    final List<RequirementProvider> providers = holder.annotations().stream()
      .flatMap(anno -> RequirementRegistry.get().getProvider(anno.source()).stream())
      .distinct()
      .toList();
    if (!providers.isEmpty()) {
      node.setAttribute(AttributeKey.REQUIREMENT_PROVIDER, new CombinedRequirementProvider(providers));
    }
  }

  //
  // Not parseable.
  //

  @Override
  public CommandNode visitInterface(SourceInterface sourceInterface, Void unused) {
    throw new IllegalStateException("Cannot parse interface");
  }

  @Override
  public CommandNode visitConstructor(SourceConstructor sourceConstructor, Void unused) {
    throw new IllegalStateException("Cannot parse constructor.");
  }

  @Override
  public CommandNode visitAnnotationInterface(SourceAnnotationInterface sourceAnnotationInterface, Void unused) {
    throw new IllegalStateException("Cannot parse annotation class.");
  }

  //
  // Misc.
  //

  private void applyExecutorTransform(AnnotationsHolder element, Attributable node) {
    if (element.hasAnnotationInherited(UnsetExecutorWrapper.class)) {
      node.setAttribute(AttributeKey.EXECUTOR_WRAPPER_UNSET, true);
      return;
    }

    this.applyRegistrableProvider(
      node,
      element,
      ExecutorWrapperRegistry.get(),
      AttributeKey.EXECUTOR_WRAPPER,
      "executor wrapper"
    );
  }

  private CommandParameter parseParameter(SourceParameterLike parameter) {
    debug("| Parsing parameter: " + parameter.name());

    if (!PlatformUtils.get().mayParameterBeArgument(parameter)) {
      return new UnparsedCommandParameter(parameter);
    }

    if (parameter.hasAnnotationInherited(Literal.class)) {
      final Literal literal = parameter.firstAnnotationByType(Literal.class).value(Literal.class);
      final String[] declared = literal.value();
      if (declared.length == 0) {
        return LiteralCommandArgument.literal(parameter.name());
      } else if (declared.length == 1) {
        return LiteralCommandArgument.literal(declared[0]);
      } else {
        return MultiLiteralCommandArgument.multiLiteral(Set.of(declared));
      }
    }

    final BrigadierArgumentType argumentType;
    try {
      argumentType = BrigadierArgumentConverter.get().getAsArgumentType(parameter);
    } catch (ConversionException e) {
      return new UnparsedCommandParameter(parameter);
    }

    debug("  | Successfully found Brigadier type: %s", argumentType);
    final RequiredCommandArgument commandArgument = RequiredCommandArgument.of(argumentType, parameter.name());
    applyRegistrableProvider(commandArgument, parameter, SuggestionsRegistry.get(), AttributeKey.SUGGESTION_PROVIDER, "suggestion");
    return commandArgument;
  }

  private <T> void applyRegistrableProvider(
    Attributable attributable,
    AnnotationsHolder element,
    RegistrableRegistry<T> registry,
    AttributeKey<T> key,
    String name
  ) {
    boolean found = false;
    for (SourceAnnotation annotation : element.annotations()) {
      final Optional<T> provider = registry.getProvider(annotation.source());
      if (provider.isPresent()) {
        if (found) {
          this.infoSource("Multiple %s providers has been declared", element, name);
        } else {
          attributable.setAttribute(key, provider.get());
          found = true;
        }
      }
    }
  }
}
