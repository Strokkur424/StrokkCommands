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
package net.strokkur.commands.internal.arguments;

import net.strokkur.commands.arguments.DoubleArg;
import net.strokkur.commands.arguments.FloatArg;
import net.strokkur.commands.arguments.IntArg;
import net.strokkur.commands.arguments.LongArg;
import net.strokkur.commands.arguments.StringArg;
import net.strokkur.commands.arguments.StringArgType;
import net.strokkur.commands.internal.exceptions.ParameterArgumentException;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToType;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.MethodInvocationBuilder;
import net.strokkur.jap.code.type.CodePrimitiveType;
import net.strokkur.jap.code.type.CodeType;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.source.annotation.SourceAnnotation;
import net.strokkur.jap.source.classmodel.SourceParameterLike;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BrigadierArgumentConverter implements ForwardingMessagerWrapper {
  protected final Map<CodeType, BiFunction<SourceParameterLike, String, BrigadierArgumentType>> conversionMap;

  public BrigadierArgumentConverter() {
    this.conversionMap = new HashMap<>();
    initializeArguments();
  }

  public static BrigadierArgumentConverter get() {
    class Holder {
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
      static final Optional<BrigadierArgumentConverter> INSTANCE = ServiceLoader.load(
        BrigadierArgumentConverter.class, BrigadierArgumentConverter.class.getClassLoader()
      ).findFirst();
    }

    return Holder.INSTANCE.orElseThrow(() -> new RuntimeException("No instance of BrigadierArgumentConverter found."));
  }

  protected @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(
    String argumentName,
    CodeType type,
    SourceParameterLike parameter
  ) throws ParameterArgumentException {
    return null;
  }

  private Function<SourceAnnotation, ConvertToExpression> minMaxValued(
    MethodInvocationBuilder builder,
    ConvertToExpression defaultMin
  ) {
    return a -> {
      final ConvertToExpression minExpr = a.isSet("min")
        ? a.parameter("min").expression()
        : defaultMin;

      if (a.isSet("max")) {
        return builder
          .addParameters(
            minExpr,
            a.parameter("max").expression()
          );
      }

      if (a.isSet("min")) {
        return builder.addParameters(minExpr);
      }

      return builder;
    };
  }

  protected void initializeArguments() {
    putFor((unused, name) -> BrigadierArgumentType.of(
      "boolean",
      Classes.BOOL_ARGUMENT_TYPE.chainMethod("bool"),
      Classes.BOOL_ARGUMENT_TYPE.chainMethod("getBool")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), CodePrimitiveType.BOOL, CodePrimitiveType.BOOL.boxed());

    putFor((p, name) -> annotatedOr(p, IntArg.class,
      "int",
      minMaxValued(
        Classes.INTEGER_ARGUMENT_TYPE.chainMethod("integer"),
        JavaTypes.INTEGER.chainField("MIN_VALUE")
      ),
      Classes.INTEGER_ARGUMENT_TYPE.chainMethod("integer"),
      Classes.INTEGER_ARGUMENT_TYPE.chainMethod("getInteger")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), CodePrimitiveType.INT, CodePrimitiveType.INT.boxed());

    putFor((p, name) -> annotatedOr(p, LongArg.class,
      "long",
      minMaxValued(
        Classes.LONG_ARGUMENT_TYPE.chainMethod("longArg"),
        JavaTypes.LONG.chainField("MIN_VALUE")
      ),
      Classes.LONG_ARGUMENT_TYPE.chainMethod("longArg"),
      Classes.LONG_ARGUMENT_TYPE.chainMethod("getLong")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), CodePrimitiveType.LONG, CodePrimitiveType.LONG.boxed());

    putFor((p, name) -> annotatedOr(p, FloatArg.class,
      "float",
      minMaxValued(
        Classes.FLOAT_ARGUMENT_TYPE.chainMethod("floatArg"),
        JavaTypes.FLOAT.chainField("MAX_VALUE").negate()
      ),
      Classes.FLOAT_ARGUMENT_TYPE.chainMethod("floatArg"),
      Classes.FLOAT_ARGUMENT_TYPE.chainMethod("getFloat")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), CodePrimitiveType.FLOAT, CodePrimitiveType.FLOAT.boxed());

    putFor((p, name) -> annotatedOr(p, DoubleArg.class,
      "double",
      minMaxValued(
        Classes.DOUBLE_ARGUMENT_TYPE.chainMethod("doubleArg"),
        JavaTypes.DOUBLE.chainField("MAX_VALUE").negate()
      ),
      Classes.DOUBLE_ARGUMENT_TYPE.chainMethod("doubleArg"),
      Classes.DOUBLE_ARGUMENT_TYPE.chainMethod("getDouble")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), CodePrimitiveType.DOUBLE, CodePrimitiveType.DOUBLE.boxed());

    putFor((p, name) -> annotatedOr(p, StringArg.class,
      "string",
      a -> Classes.STRING_ARGUMENT_TYPE.chainMethod(a.value(StringArg.class).value().getBrigadierType()),
      Classes.STRING_ARGUMENT_TYPE.chainMethod(StringArgType.WORD.getBrigadierType()),
      Classes.STRING_ARGUMENT_TYPE.chainMethod("getString")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), JavaTypes.STRING);
  }

  protected final void putFor(BiFunction<SourceParameterLike, String, BrigadierArgumentType> value, List<? extends ConvertToType> types) {
    for (ConvertToType key : types) {
      conversionMap.put(key.toType(), value);
    }
  }

  protected final void putFor(BiFunction<SourceParameterLike, String, BrigadierArgumentType> value, ConvertToType... types) {
    putFor(value, Arrays.asList(types));
  }

  protected final <T extends Annotation> BrigadierArgumentType annotatedOr(
    SourceParameterLike variable,
    Class<T> annotation,
    String argumentTypeName,
    Function<SourceAnnotation, ConvertToExpression> withAnnotation,
    ConvertToExpression withoutAnnotation,
    ConvertToExpression retrieval
  ) {
    final Classes annotationType = Classes.ofClass(annotation);
    if (variable.hasAnnotation(annotationType)) {
      return BrigadierArgumentType.of(argumentTypeName, withAnnotation.apply(variable.getAnnotation(annotationType)), retrieval);
    }

    return BrigadierArgumentType.of(argumentTypeName, withoutAnnotation, retrieval);
  }

  public final BrigadierArgumentType getAsArgumentType(SourceParameterLike parameter) throws ParameterArgumentException {
    final String argumentName = parameter.name();
    final CodeType type = parameter.type().toType();

    debug("Parsing parameter: " + parameter);

    final BrigadierArgumentType customArg = handleCustomArgumentAnnotations(argumentName, type, parameter);
    if (customArg != null) {
      return customArg;
    }

    if (!conversionMap.containsKey(type)) {
      throw new ParameterArgumentException("Cannot find Brigadier equivalent for argument of type %s.".formatted(type));
    }

    final BrigadierArgumentType out = Optional.ofNullable(conversionMap.get(type)).map(it -> it.apply(parameter, argumentName)).orElse(null);
    if (out != null) {
      return out;
    }

    throw new ParameterArgumentException("An unexpected error occurred whilst converting type %s to Brigadier equivalent.".formatted(type));
  }
}
