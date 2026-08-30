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
import net.strokkur.commands.container.ManyDefaultExecutes;
import net.strokkur.commands.container.ManyExecutes;
import net.strokkur.commands.container.ManySubcommands;
import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.exceptions.IllegalCommandClassTypeException;
import net.strokkur.commands.internal.exceptions.IllegalSubcommandFieldType;
import net.strokkur.commands.internal.exceptions.ParameterArgumentException;
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
import net.strokkur.jap.source.classmodel.SourceEnum;
import net.strokkur.jap.source.classmodel.SourceField;
import net.strokkur.jap.source.classmodel.SourceInterface;
import net.strokkur.jap.source.classmodel.SourceMethod;
import net.strokkur.jap.source.classmodel.SourceParameterLike;
import net.strokkur.jap.source.classmodel.SourceRecord;
import net.strokkur.jap.source.type.ClassLikeType;
import net.strokkur.jap.source.visitor.SourceVisitor;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class CommandParsingSourceVisitor implements SourceVisitor<CommandNode, CommandParsingSourceVisitor.ParsingContext>, ForwardingMessagerWrapper {
  private static final CommandParsingSourceVisitor INSTANCE = new CommandParsingSourceVisitor();

  public static CommandParsingSourceVisitor get() {
    return INSTANCE;
  }

  private static ParsingContext newCtx() {
    return new ParsingContext();
  }

  //
  // Methods unique to this visitor for better DX when parsing commands.
  //

  /// Handles [Command]-annotated classes instead of the standard [Subcommand].
  public CommandNode visitCommandClass(SourceClassLike classLike) {
    final CommandNode node = switch (classLike) {
      case SourceClass sourceClass -> visitClass(sourceClass, newCtx(), null, Command.class, Command::value);
      case SourceRecord record -> visitRecord(record, newCtx(), null, Command.class, Command::value);
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
  public CommandNode visitClass(SourceClass sourceClass, ParsingContext ctx) {
    return visitClass(sourceClass, ctx, ManySubcommands.class, Subcommand.class, Subcommand::value);
  }

  @Override
  public CommandNode visitRecord(SourceRecord sourceRecord, ParsingContext ctx) {
    return visitRecord(sourceRecord, ctx, ManySubcommands.class, Subcommand.class, Subcommand::value);
  }

  @Override
  public CommandNode visitMethod(SourceMethod sourceMethod, ParsingContext ctx) {
    final List<CommandParameter> arguments = sourceMethod.parameters().stream()
      .map(this::parseParameter)
      .toList();
    final List<CommandArgument> commandArguments = arguments.stream()
      .filter(CommandArgument.class::isInstance)
      .map(CommandArgument.class::cast)
      .toList();

    final CommandNode rootNode = CommandNode.createEmpty();
    final Executable executable = new Executable(sourceMethod.enclosed(), sourceMethod, arguments);

    final AdvancedNodeExtender<Executes> executesExtender = new AdvancedNodeExtender<>(Executes.class, Executes::value)
      .withPluralAnnotationsClass(ManyExecutes.class)
      .withAlwaysRunHooks(false)
      .withFirstPathNodeConsumer(node -> {
        applyExecutorTransform(sourceMethod, node);
        PlatformUtils.get().populateNode(sourceMethod, node);
        applyRequirements(sourceMethod, node);
      })
      .withPostProcess(node -> {
        final CommandNode endNode = node.addArguments(commandArguments);
        final Executable executableObj = new Executable(executable);
        endNode.setAttribute(AttributeKey.EXECUTABLE, executableObj);
        PlatformUtils.get().populateExecutesNode(executableObj, endNode, arguments);
      });

    executesExtender.accept(sourceMethod, rootNode);

    final AdvancedNodeExtender<DefaultExecutes> defaultExecutesExtender = executesExtender
      .withAnnotationClass(DefaultExecutes.class, DefaultExecutes::value)
      .withPluralAnnotationsClass(ManyDefaultExecutes.class)
      .withPostProcess(node -> {
        final CommandNode endNode = node.addArguments(commandArguments);
        final DefaultExecutable executableObj = new DefaultExecutable(executable);
        endNode.setAttribute(AttributeKey.DEFAULT_EXECUTABLE, executableObj);
        PlatformUtils.get().populateExecutesNode(executableObj, endNode, arguments);
      });

    defaultExecutesExtender.accept(sourceMethod, rootNode);
    return rootNode;
  }

  @Override
  public CommandNode visitField(SourceField sourceField, ParsingContext ctx) {
    if (!(sourceField.type() instanceof ClassLikeType type)) {
      throw new IllegalSubcommandFieldType(sourceField.type());
    }

    ctx.isCurrentlyParsingField = true;
    final CommandNode nestedNode = type.like().accept(this, ctx);

    final AdvancedNodeExtender<Subcommand> fieldExtender = new AdvancedNodeExtender<>(Subcommand.class, Subcommand::value)
      .withPluralAnnotationsClass(ManySubcommands.class)
      .withFirstPathNodeConsumer(node -> {
        node.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(sourceField));
        applyExecutorTransform(sourceField, node);
        PlatformUtils.get().populateNode(sourceField, node);
        applyRequirements(sourceField, node);
      })
      .withPostProcess(node -> node.addChild(nestedNode));

    final CommandNode rootNode = CommandNode.createEmpty();
    fieldExtender.accept(sourceField, rootNode);
    return rootNode;
  }

  //
  // Util/common methods.
  //

  private <A extends Annotation> CommandNode visitClass(
    SourceClass sourceClass, ParsingContext ctx,
    @Nullable Class<? extends Annotation> collectionAnnotation, Class<A> annotationClass, Function<A, String> toPath
  ) {
    final boolean nestedField = ctx.isCurrentlyParsingField;
    ctx.isCurrentlyParsingField = false;

    // Parse nested elements first, so that the same CommandNode instances can be reused inside the
    // final node consumers.
    final List<CommandNode> nestedNodes = new ArrayList<>();
    nestedNodes.addAll(sourceClass.methods().stream()
      .filter(method -> method.hasAnnotations(ManyExecutes.class, Executes.class)
        || method.hasAnnotations(ManyDefaultExecutes.class, DefaultExecutes.class)
      )
      .map(method -> method.accept(this, ctx))
      .toList());
    nestedNodes.addAll(sourceClass.fields().stream()
      .filter(field -> field.hasAnnotations(ManySubcommands.class, Subcommand.class))
      .map(field -> field.accept(this, ctx))
      .toList());
    nestedNodes.addAll(sourceClass.nestedClasses().stream()
      .filter(nested -> nested.hasAnnotations(ManySubcommands.class, Subcommand.class))
      .map(nested -> nested.accept(this, ctx))
      .toList());

    final AdvancedNodeExtender<A> classExtender = new AdvancedNodeExtender<>(annotationClass, toPath)
      .withPluralAnnotationsClass(collectionAnnotation)
      .withFirstPathNodeConsumer(node -> {
        applyExecutorTransform(sourceClass, node);
        PlatformUtils.get().populateNode(sourceClass, node);
        applyRequirements(sourceClass, node);
      })
      .withPostProcess(node -> nestedNodes.forEach(node::addChild));

    final CommandNode rootNode = CommandNode.createEmpty();
    if (!nestedField) {
      rootNode.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(sourceClass));
    }

    classExtender.accept(sourceClass, rootNode);
    return rootNode;
  }

  private <A extends Annotation> CommandNode visitRecord(
    SourceRecord record, ParsingContext ctx,
    @Nullable Class<? extends Annotation> collectionAnnotation, Class<A> annotationClass, Function<A, String> toPath
  ) {
    final boolean nestedField = ctx.isCurrentlyParsingField;
    ctx.isCurrentlyParsingField = false;

    // Parse nested elements first, so that the same CommandNode instances can be reused inside the
    // final node consumers.
    final List<CommandNode> nestedNodes = new ArrayList<>();
    nestedNodes.addAll(record.methods().stream()
      .filter(method -> method.hasAnnotations(ManyExecutes.class, Executes.class)
        || method.hasAnnotations(ManyDefaultExecutes.class, DefaultExecutes.class))
      .map(method -> method.accept(this, ctx))
      .toList());
    nestedNodes.addAll(record.nestedClasses().stream()
      .filter(nested -> nested.hasAnnotations(ManySubcommands.class, Subcommand.class))
      .map(nested -> nested.accept(this, ctx))
      .toList());

    final List<CommandParameter> parsedComponents = record.components().stream()
      .map(this::parseParameter)
      .toList();

    final List<CommandArgument> recordArguments = parsedComponents.stream()
      .filter(CommandArgument.class::isInstance)
      .map(CommandArgument.class::cast)
      .toList();

    final AdvancedNodeExtender<A> recordExtender = new AdvancedNodeExtender<>(annotationClass, toPath)
      .withPluralAnnotationsClass(collectionAnnotation)
      .withFirstPathNodeConsumer(node -> {
        applyExecutorTransform(record, node);
        PlatformUtils.get().populateNode(record, node);
        applyRequirements(record, node);
      })
      .withEndPathNodeTransform(node -> node.addArguments(recordArguments))
      .withPostProcess(node -> nestedNodes.forEach(node::addChild));

    final CommandNode rootNode = CommandNode.createEmpty();
    if (!nestedField) {
      rootNode.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(record));
    }
    rootNode.setAttribute(AttributeKey.RECORD_ARGUMENTS, RecordArguments.of(record, parsedComponents));

    recordExtender.accept(record, rootNode);
    return rootNode;
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
  public CommandNode visitInterface(SourceInterface sourceInterface, ParsingContext ctx) {
    throw new IllegalStateException("Cannot parse interface.");
  }

  @Override
  public CommandNode visitEnum(SourceEnum sourceEnum, ParsingContext data) {
    throw new IllegalStateException("Cannot parse enum.");
  }

  @Override
  public CommandNode visitConstructor(SourceConstructor sourceConstructor, ParsingContext ctx) {
    throw new IllegalStateException("Cannot parse constructor.");
  }

  @Override
  public CommandNode visitAnnotationInterface(SourceAnnotationInterface sourceAnnotationInterface, ParsingContext ctx) {
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
    if (!PlatformUtils.get().mayParameterBeArgument(parameter)) {
      return new UnparsedCommandParameter(parameter);
    }

    if (parameter.hasAnnotationInherited(Literal.class)) {
      final Literal literal = parameter.getAnnotationValueInherited(Literal.class);
      final String[] declared = literal.value();
      if (declared.length == 0) {
        return LiteralCommandArgument.literal(parameter.name(), true);
      } else if (declared.length == 1) {
        return LiteralCommandArgument.literal(declared[0], true);
      } else {
        return MultiLiteralCommandArgument.multiLiteral(Set.of(declared));
      }
    }

    final BrigadierArgumentType argumentType;
    try {
      argumentType = BrigadierArgumentConverter.get().getAsArgumentType(parameter);
    } catch (ParameterArgumentException e) {
      return new UnparsedCommandParameter(parameter);
    }

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

  public static class ParsingContext {
    private boolean isCurrentlyParsingField = false;
  }
}
