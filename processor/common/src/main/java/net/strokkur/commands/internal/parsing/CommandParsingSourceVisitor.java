package net.strokkur.commands.internal.parsing;

import net.strokkur.commands.Command;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.internal.NodeUtils;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.exceptions.IllegalCommandClassTypeException;
import net.strokkur.commands.internal.exceptions.IllegalSubcommandFieldType;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.executable.CommandParameter;
import net.strokkur.commands.internal.intermediate.executable.DefaultExecutable;
import net.strokkur.commands.internal.intermediate.executable.Executable;
import net.strokkur.commands.internal.intermediate.registrable.CombinedRequirementProvider;
import net.strokkur.commands.internal.intermediate.registrable.RequirementProvider;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.jap.source.annotation.AnnotationsHolder;
import net.strokkur.jap.source.classmodel.SourceAnnotationInterface;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceConstructor;
import net.strokkur.jap.source.classmodel.SourceField;
import net.strokkur.jap.source.classmodel.SourceInterface;
import net.strokkur.jap.source.classmodel.SourceMethod;
import net.strokkur.jap.source.classmodel.SourceRecord;
import net.strokkur.jap.source.util.MessagerWrapper;
import net.strokkur.jap.source.visitor.SourceVisitor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandParsingSourceVisitor implements SourceVisitor<CommandNode, Void>, ForwardingMessagerWrapper {
  private final MessagerWrapper messager;
  protected final NodeUtils utils;

  public CommandParsingSourceVisitor(MessagerWrapper messager, NodeUtils utils) {
    this.messager = messager;
    this.utils = utils;
  }

  //
  // Methods unique to this visitor for better DX when parsing commands.
  //

  /// Handles [Command]-annotated classes instead of the standard [Subcommand].
  public CommandNode visitCommandClass(SourceClassLike classLike, Void unused) {
    return switch (classLike) {
      case SourceClass sourceClass -> visitClass(sourceClass, unused, Command.class, Command::value);
      case SourceRecord record -> visitRecord(record, unused, Command.class, Command::value);
      default -> throw new IllegalCommandClassTypeException(classLike);
    };
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
    List<CommandParameter> arguments = sourceMethod.parameters().stream()
      .map(utils::parseParameter)
      .toList();
    List<CommandArgument> commandArguments = arguments.stream()
      .filter(CommandArgument.class::isInstance)
      .map(CommandArgument.class::cast)
      .toList();

    CommandNode rootNode = CommandNode.createEmpty();
    Executable executable = new Executable(sourceMethod.enclosed(), sourceMethod, arguments);

    applyExecutesLogic(
      sourceMethod, rootNode,
      Executes.class, Executes::value,
      arguments, commandArguments,
      new Executable(executable), AttributeKey.EXECUTABLE
    );
    applyExecutesLogic(
      sourceMethod, rootNode,
      DefaultExecutes.class, DefaultExecutes::value,
      arguments, commandArguments,
      new DefaultExecutable(executable), AttributeKey.DEFAULT_EXECUTABLE
    );

    // Apply attributes
    utils.applyExecutorTransform(sourceMethod, rootNode);
    utils.platformUtils().populateNode(sourceMethod, rootNode);
    applyRequirements(sourceMethod, rootNode);

    return rootNode;
  }

  @Override
  public CommandNode visitField(SourceField sourceField, Void unused) {
    CommandNode nestedNode = switch (sourceField.type()) {
      case SourceClass sourceClass -> sourceClass.accept(this, unused);
      case SourceRecord record -> record.accept(this, unused);
      default -> throw new IllegalSubcommandFieldType(sourceField.type());
    };

    CommandNode rootNode = CommandNode.createEmpty();
    forEachPathAnnotation(
      sourceField, rootNode,
      Subcommand.class, Subcommand::value,
      (node) -> node.addChild(nestedNode)
    );

    // Apply attribute modifiers
    rootNode.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(sourceField));
    utils.applyExecutorTransform(sourceField, rootNode);
    utils.platformUtils().populateNode(sourceField, rootNode);
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
    List<CommandNode> nestedNodes = new ArrayList<>();
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

    CommandNode rootNode = CommandNode.createEmpty();
    forEachPathAnnotation(
      sourceClass, rootNode,
      annotationClass, toPath,
      (node) -> nestedNodes.forEach(node::addChild)
    );

    // Apply some attributes to the root node before returning it.
    rootNode.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(sourceClass));
    utils.applyExecutorTransform(sourceClass, rootNode);
    utils.platformUtils().populateNode(sourceClass, rootNode);
    applyRequirements(sourceClass, rootNode);

    return rootNode;
  }

  private <A extends Annotation> CommandNode visitRecord(
    SourceRecord record, Void unused,
    Class<A> annotationClass, Function<A, String> toPath
  ) {
    // Parse nested elements first, so that the same CommandNode instances can be reused inside the
    // final node consumers.
    List<CommandNode> nestedNodes = new ArrayList<>();
    nestedNodes.addAll(record.methods().stream()
      .filter(method -> method.hasAnnotationInherited(Executes.class) || method.hasAnnotationInherited(DefaultExecutes.class))
      .map(method -> method.accept(this, unused))
      .toList());
    nestedNodes.addAll(record.nestedClasses().stream()
      .filter(nested -> nested.hasAnnotationInherited(Subcommand.class))
      .map(nested -> nested.accept(this, unused))
      .toList());

    CommandNode rootNode = CommandNode.createEmpty();
    List<CommandParameter> parsedComponents = record.components().stream()
      .map(utils::parseParameter)
      .toList();

    CommandNode postArgumentsNode = rootNode.addArguments(parsedComponents.stream()
      .filter(CommandArgument.class::isInstance)
      .map(CommandArgument.class::cast)
      .toList());

    forEachPathAnnotation(
      record, postArgumentsNode,
      annotationClass, toPath,
      (node) -> nestedNodes.forEach(node::addChild)
    );

    // Apply some attributes to the root node before returning it.
    rootNode.setAttribute(AttributeKey.ACCESS, ExecuteAccess.of(record));
    rootNode.setAttribute(AttributeKey.RECORD_ARGUMENTS, parsedComponents);
    utils.applyExecutorTransform(record, rootNode);
    utils.platformUtils().populateNode(record, rootNode);
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
      (node) -> {
        CommandNode endNode = node.addArguments(commandArguments);
        endNode.setAttribute(key, executable);
        utils.platformUtils().populateExecutesNode(executable, endNode, arguments);
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
        A anno = a.value(annotationClass);
        return toPath.apply(anno);
      })
      .distinct()
      .map(path -> Arrays.stream(path.split(" "))
        .map(LiteralCommandArgument::new)
        .toList())
      .forEach(args -> {
        CommandNode endNode = root.addArguments(args);
        endNodeConsumer.accept(endNode);
      });
  }

  private void applyRequirements(AnnotationsHolder holder, CommandNode node) {
    List<RequirementProvider> providers = holder.annotations().stream()
      .flatMap(anno -> utils.requirementRegistry().getProvider(anno.source()).stream())
      .distinct()
      .toList();
    node.setAttribute(AttributeKey.REQUIREMENT_PROVIDER, new CombinedRequirementProvider(providers));
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

  @Override
  public MessagerWrapper delegateMessager() {
    return this.messager;
  }
}
