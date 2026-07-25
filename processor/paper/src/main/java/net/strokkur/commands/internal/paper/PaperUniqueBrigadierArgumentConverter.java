package net.strokkur.commands.internal.paper;

import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.exceptions.ParameterArgumentException;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.paper.arguments.AngleArg;
import net.strokkur.commands.paper.arguments.CustomArg;
import net.strokkur.commands.paper.arguments.TimeArg;
import net.strokkur.jap.code.convert.ConvertToClassType;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.MethodInvocationBuilder;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.type.CodePrimitiveType;
import net.strokkur.jap.code.type.CodeType;
import net.strokkur.jap.code.type.CodeTypes;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.source.annotation.SourceAnnotation;
import net.strokkur.jap.source.classmodel.SourceParameterLike;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;

/// This class handles special argument type cases, which would be somewhat of a drag to try and generate
/// automatically. [PaperBrigadierArgumentConverter] holds the real argument converter that gets used.
@SuppressWarnings("SameParameterValue")
public abstract class PaperUniqueBrigadierArgumentConverter extends BrigadierArgumentConverter {
  private static final Classes ARGUMENT_TYPES = Classes.create("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
  private static final Classes REGISTRY_KEY = Classes.create("io.papermc.paper.registry.RegistryKey");
  private static final Classes REGISTRY_ARGUMENT_EXTRACTOR = Classes.create("io.papermc.paper.command.brigadier.argument.RegistryArgumentExtractor");

  private static final Classes ANGLE_RESOLVER = Classes.create("io.papermc.paper.command.brigadier.argument.resolvers.AngleResolver");
  private static final Classes SIGNED_ARGUMENT_RESOLVER = Classes.create("io.papermc.paper.command.brigadier.argument.SignedArgumentResolver");

  @Override
  protected @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(String argumentName, CodeType type, SourceParameterLike parameter) {
    if (parameter.hasAnnotation(CustomArg.class)) {
      final SourceAnnotation customArg = parameter.firstAnnotationByType(CustomArg.class);
      final CodeClassType classType = customArg.parameter("value").classValue();
      return BrigadierArgumentType.of(
        "custom-arg-" + customArg.type(),
        classType.ctor(),
        Expressions.variable("ctx").chainMethod("getArgument",
          Expressions.string(argumentName),
          classType.chainField("class")
        )
      );
    }

    // - time()                java.lang.Integer
    // - time(int)             java.lang.Integer
    if (parameter.hasAnnotation(TimeArg.class)) {
      if (type != CodePrimitiveType.INT && type.isType(JavaTypes.INTEGER)) {
        throw new ParameterArgumentException("@TimeArg must be of type 'int'");
      }

      final SourceAnnotation timeArg = parameter.firstAnnotationByType(TimeArg.class);

      return BrigadierArgumentType.of(
        "time",
        timeArg.isSet("value") ?
          ARGUMENT_TYPES.chainMethod("time", timeArg.parameter("value").expression()) :
          ARGUMENT_TYPES.chainMethod("time"),
        Classes.INTEGER_ARGUMENT_TYPE.chainMethod("getInteger",
          Expressions.variable("ctx"),
          Expressions.string(argumentName)
        )
      );
    }

    // - angle()               io.papermc.paper.command.brigadier.argument.resolvers.AngleResolver
    if (parameter.hasAnnotation(AngleArg.class)) {
      if (type != CodePrimitiveType.FLOAT && type.isType(JavaTypes.FLOAT)) {
        throw new ParameterArgumentException("@AngleArg must be of type 'float'");
      }

      return BrigadierArgumentType.of(
        "angle",
        ARGUMENT_TYPES.chainMethod("float"),
        Expressions.variable("ctx")
          .chainMethod("getArgument",
            Expressions.string(argumentName),
            ANGLE_RESOLVER.chainField("class")
          )
          .chainMethod("resolve",
            Expressions.variable("ctx").chainMethod("getSource")
          )
      );
    }

    return null;
  }

  @Override
  protected void initializeArguments() {
    super.initializeArguments();

    // - signedMessage()       io.papermc.paper.command.brigadier.argument.SignedMessageResolver
    putSimple("signedMessage", SIGNED_ARGUMENT_RESOLVER.toClassType().identifiableName());
    putFor((p, name) -> BrigadierArgumentType.of(
      "signedMessage",
      ARGUMENT_TYPES.chainMethod("signedMessage"),
      Expressions.variable("ctx").chainMethod(
        "getArgument",
        Expressions.string(name),
        SIGNED_ARGUMENT_RESOLVER.chainField("class")
      ).chainMethod(
        "resolveSignedMessage",
        Expressions.string(name),
        Expressions.variable("ctx")
      )
    ), JavaTypes.COMPLETABLE_FUTURE.typed(CodeTypes.ofClass("net.kyori.adventure.chat.SignedMessage")));
  }

  private MethodInvocationBuilder resolveExpr(String name, ConvertToClassType unresolved) {
    return Expressions.variable("ctx").chainMethod(
      "getArgument",
      Expressions.string(name),
      unresolved.chainField("class")
    ).chainMethod(
      "resolve",
      Expressions.variable("ctx").chainMethod("getSource")
    );
  }

  protected void putResolver(String methodName, ConvertToClassType unresolved, ConvertToClassType resolved) {
    putSimple(methodName, unresolved.toClassType().identifiableName());
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved)
    ), resolved);
  }

  protected void putResolverValued(String methodName, ConvertToClassType unresolved, ConvertToClassType resolved, Class<? extends Annotation> anno) {
    putSimple(methodName, unresolved.toClassType().identifiableName());
    putFor((p, name) -> {
      final ConvertToExpression arg = p.hasAnnotation(anno) ?
        p.firstAnnotationByType(anno).parameter("value").expression() :
        null;
      final MethodInvocationBuilder method = ARGUMENT_TYPES.chainMethod(methodName);
      return BrigadierArgumentType.of(
        methodName,
        arg != null ? method.addParameters(arg) : method,
        resolveExpr(name, unresolved)
      );
    }, resolved);
  }

  protected void putListResolver(String methodName, ConvertToClassType unresolved, ConvertToClassType resolved) {
    putSimple(methodName, unresolved.toClassType().identifiableName());

    // List<T>
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved)
    ), JavaTypes.LIST.typed(resolved));
    // List<T> -> Collection<T>
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved)
    ), JavaTypes.COLLECTION.typed(resolved));
    // T[]
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved).chainMethod("toArray", resolved.toArray().methodReference("new"))
    ), resolved.toArray());
  }

  protected void putListToOneResolver(String methodName, ConvertToClassType unresolved, ConvertToClassType resolved) {
    putSimple(methodName, unresolved.toClassType().identifiableName());

    // List<T> -> T
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved).chainMethod("getFirst")
    ), resolved);
  }

  protected void putCollectionResolver(String methodName, ConvertToClassType unresolved, ConvertToClassType resolved) {
    putSimple(methodName, unresolved.toClassType().identifiableName());

    // Collection<T>
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved)
    ), JavaTypes.COLLECTION.typed(resolved));
    // Collection<T> -> List<T>
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved).chainMethod("stream").chainMethod("toList")
    ), JavaTypes.LIST.typed(resolved));
    // Collection<T> -> T
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved).chainMethod("stream").chainMethod("findFirst").chainMethod("orElseThrow")
    ), resolved);
    // T[]
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      resolveExpr(name, unresolved).chainMethod("toArray", resolved.toArray().methodReference("new"))
    ), resolved.toArray());
  }

  protected void putSimple(String methodName, String returnTypeFqn) {
    final CodeClassType returnType = CodeTypes.ofClass(returnTypeFqn);
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      Expressions.variable("ctx").chainMethod(
        "getArgument",
        Expressions.string(name),
        returnType.withoutGenerics().chainField("class")
      )
    ), returnType);
  }

  protected void putRange(String methodName, String returnTypeFqn, String rangeType) {
    final CodeClassType returnType = CodeTypes.ofClass(returnTypeFqn);
    putFor((p, name) -> BrigadierArgumentType.of(
      methodName,
      ARGUMENT_TYPES.chainMethod(methodName),
      Expressions.variable("ctx").chainMethod(
        "getArgument",
        Expressions.string(name),
        returnType.withoutGenerics().chainField("class")
      ).chainMethod("range")
    ), CodeTypes.ofClass("com.google.common.collect.Range").typed(CodeTypes.ofClass(rangeType)));
  }

  protected void putRegistry(String name, CodeClassType type) {
    putFor((p, argName) -> BrigadierArgumentType.of(
      name,
      ARGUMENT_TYPES.chainMethod("resource", REGISTRY_KEY.chainField(name)),
      Expressions.variable("ctx").chainMethod(
        "getArgument",
        Expressions.string(argName),
        type.withoutGenerics().chainField("class")
      )
    ), type);
    putFor((p, argName) -> BrigadierArgumentType.of(
      name,
      ARGUMENT_TYPES.chainMethod("resourceKey", REGISTRY_KEY.chainField(name)),
      REGISTRY_ARGUMENT_EXTRACTOR.chainMethod(
        "getTypedKey",
        Expressions.variable("ctx"),
        REGISTRY_KEY.chainField(name),
        Expressions.string(argName)
      )
    ), REGISTRY_KEY.typed(type));
  }
}
