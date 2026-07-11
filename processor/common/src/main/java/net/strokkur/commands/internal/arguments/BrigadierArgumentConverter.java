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
import net.strokkur.commands.internal.exceptions.ConversionException;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.jap.code.convert.ConvertToExpression;
import net.strokkur.jap.code.convert.ConvertToType;
import net.strokkur.jap.code.expression.Expressions;
import net.strokkur.jap.code.expression.builder.MethodInvocationBuilder;
import net.strokkur.jap.code.type.CodePrimitiveType;
import net.strokkur.jap.code.type.CodeType;
import net.strokkur.jap.code.type.preset.JavaTypes;
import net.strokkur.jap.source.annotation.SourceAnnotation;
import net.strokkur.jap.source.classmodel.SourceMethodParameter;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BrigadierArgumentConverter implements ForwardingMessagerWrapper {
  private final MessagerWrapper messagerWrapper;
  protected final Map<CodeType, BiFunction<SourceMethodParameter, String, BrigadierArgumentType>> conversionMap;

  public BrigadierArgumentConverter(MessagerWrapper messagerWrapper) {
    this.messagerWrapper = messagerWrapper;
    this.conversionMap = new HashMap<>();
    initializeArguments();
  }

  protected @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(
    String argumentName,
    CodeType type,
    SourceMethodParameter parameter
  ) throws ConversionException {
    return null;
  }

  private Function<SourceAnnotation, ConvertToExpression> minMaxValued(
    MethodInvocationBuilder builder,
    ConvertToExpression defaultMin
  ) {
    return a -> {
      final ConvertToExpression minExpr = a.isSet("min")
        ? a.parameter("min").value()
        : defaultMin;

      if (a.isSet("max")) {
        return builder
          .addParameters(
            minExpr,
            a.parameter("max").value()
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
      Classes.BOOL_ARGUMENT_TYPE.chainMethod("bool"),
      Classes.BOOL_ARGUMENT_TYPE.chainMethod("getBool")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), CodePrimitiveType.BOOL, CodePrimitiveType.BOOL.boxed());

    putFor((p, name) -> annotatedOr(p, IntArg.class,
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
      minMaxValued(
        Classes.FLOAT_ARGUMENT_TYPE.chainMethod("floatArg"),
        JavaTypes.FLOAT.chainField("MAX_VALUE").unaryMinus()
      ),
      Classes.FLOAT_ARGUMENT_TYPE.chainMethod("floatArg"),
      Classes.FLOAT_ARGUMENT_TYPE.chainMethod("getFloat")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), CodePrimitiveType.FLOAT, CodePrimitiveType.FLOAT.boxed());

    putFor((p, name) -> annotatedOr(p, DoubleArg.class,
      minMaxValued(
        Classes.DOUBLE_ARGUMENT_TYPE.chainMethod("doubleArg"),
        JavaTypes.DOUBLE.chainField("MAX_VALUE").unaryMinus()
      ),
      Classes.DOUBLE_ARGUMENT_TYPE.chainMethod("doubleArg"),
      Classes.DOUBLE_ARGUMENT_TYPE.chainMethod("getDouble")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), CodePrimitiveType.DOUBLE, CodePrimitiveType.DOUBLE.boxed());

    putFor((p, name) -> annotatedOr(p, StringArg.class,
      a -> Classes.STRING_ARGUMENT_TYPE.chainMethod(a.value(StringArg.class).value().getBrigadierType()),
      Classes.STRING_ARGUMENT_TYPE.chainMethod(StringArgType.WORD.getBrigadierType()),
      Classes.STRING_ARGUMENT_TYPE.chainMethod("getString")
        .addParameters(Expressions.variable("ctx"))
        .addParameters(Expressions.string(name))
    ), JavaTypes.STRING);
  }

  protected final void putFor(BiFunction<SourceMethodParameter, String, BrigadierArgumentType> value, ConvertToType... types) {
    for (ConvertToType key : types) {
      conversionMap.put(key.toType(), value);
    }
  }

  protected final <T extends Annotation> BrigadierArgumentType annotatedOr(
    SourceMethodParameter variable,
    Class<T> annotation,
    Function<SourceAnnotation, ConvertToExpression> withAnnotation,
    ConvertToExpression withoutAnnotation,
    ConvertToExpression retrieval
  ) {
    final Classes annotationType = Classes.ofClass(annotation);
    if (variable.hasAnnotation(annotationType)) {
      return BrigadierArgumentType.of(withAnnotation.apply(variable.firstAnnotationByType(annotationType)), retrieval);
    }

    return BrigadierArgumentType.of(withoutAnnotation, retrieval);
  }

  public final BrigadierArgumentType getAsArgumentType(SourceMethodParameter parameter) throws ConversionException {
    final String argumentName = parameter.name();
    final CodeType type = parameter.type().toType();

    final BrigadierArgumentType customArg = handleCustomArgumentAnnotations(argumentName, type, parameter);
    if (customArg != null) {
      return customArg;
    }

    if (!conversionMap.containsKey(type)) {
      throw new ConversionException("Cannot find Brigadier equivalent for argument of type %s.".formatted(type));
    }

    final BrigadierArgumentType out = Optional.ofNullable(conversionMap.get(type)).map(it -> it.apply(parameter, argumentName)).orElse(null);
    if (out != null) {
      return out;
    }

    throw new ConversionException("An unexpected error occurred whilst converting type %s to Brigadier equivalent.".formatted(type));
  }

  @Override
  public final MessagerWrapper delegateMessager() {
    return this.messagerWrapper;
  }
}
