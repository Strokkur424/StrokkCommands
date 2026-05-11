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
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.codegen.CodeExpression;
import net.strokkur.commands.internal.codegen.as.AsExpression;
import net.strokkur.commands.internal.codegen.builder.Builders;
import net.strokkur.commands.internal.exceptions.ConversionException;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BrigadierArgumentConverter implements ForwardingMessagerWrapper {
  private final MessagerWrapper messagerWrapper;
  protected final Map<String, BiFunction<SourceVariable, String, BrigadierArgumentType>> conversionMap;

  public BrigadierArgumentConverter(MessagerWrapper messagerWrapper) {
    this.messagerWrapper = messagerWrapper;
    this.conversionMap = new TreeMap<>();
    initializeArguments();
  }

  protected @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(
      String argumentName,
      String type,
      SourceVariable parameter
  ) throws ConversionException {
    return null;
  }

  protected void initializeArguments() {
    putFor((unused, name) -> BrigadierArgumentType.of(
        Builders.methodInvocation("bool").setStatic(Classes.BOOL_ARGUMENT_TYPE),
        Builders.methodInvocation("getBool").setStatic(Classes.BOOL_ARGUMENT_TYPE)
            .addParameter(CodeExpression.variable("ctx"))
            .addParameter(CodeExpression.string(name))
    ), "boolean", "java.lang.Boolean");

    putFor((p, name) -> annotatedOr(p, IntArg.class,
        a -> Builders.methodInvocation("integer").setStatic(Classes.INTEGER_ARGUMENT_TYPE)
            .addParameter(CodeExpression.number(a.min()))
            .addParameter(CodeExpression.number(a.max())),
        Builders.methodInvocation("integer").setStatic(Classes.INTEGER_ARGUMENT_TYPE),
        Builders.methodInvocation("getInteger").setStatic(Classes.INTEGER_ARGUMENT_TYPE)
            .addParameter(CodeExpression.variable("ctx"))
            .addParameter(CodeExpression.string(name))
    ), "int", "java.lang.Integer");

    putFor((p, name) -> annotatedOr(p, LongArg.class,
        a -> Builders.methodInvocation("longArg").setStatic(Classes.LONG_ARGUMENT_TYPE)
            .addParameter(CodeExpression.number(a.min()))
            .addParameter(CodeExpression.number(a.max())),
        Builders.methodInvocation("longArg").setStatic(Classes.LONG_ARGUMENT_TYPE),
        Builders.methodInvocation("getLong").setStatic(Classes.LONG_ARGUMENT_TYPE)
            .addParameter(CodeExpression.variable("ctx"))
            .addParameter(CodeExpression.string(name))
    ), "long", "java.lang.Long");

    putFor((p, name) -> annotatedOr(p, FloatArg.class,
        a -> Builders.methodInvocation("floatArg").setStatic(Classes.FLOAT_ARGUMENT_TYPE)
            .addParameter(CodeExpression.number(a.min()))
            .addParameter(CodeExpression.number(a.max())),
        Builders.methodInvocation("floatArg").setStatic(Classes.FLOAT_ARGUMENT_TYPE),
        Builders.methodInvocation("getFloat").setStatic(Classes.FLOAT_ARGUMENT_TYPE)
            .addParameter(CodeExpression.variable("ctx"))
            .addParameter(CodeExpression.string(name))
    ), "float", "java.lang.Float");

    putFor((p, name) -> annotatedOr(p, DoubleArg.class,
        a -> Builders.methodInvocation("doubleArg").setStatic(Classes.DOUBLE_ARGUMENT_TYPE)
            .addParameter(CodeExpression.number(a.min()))
            .addParameter(CodeExpression.number(a.max())),
        Builders.methodInvocation("doubleArg").setStatic(Classes.DOUBLE_ARGUMENT_TYPE),
        Builders.methodInvocation("getDouble").setStatic(Classes.DOUBLE_ARGUMENT_TYPE)
            .addParameter(CodeExpression.variable("ctx"))
            .addParameter(CodeExpression.string(name))
    ), "double", "java.lang.Double");

    putFor((p, name) -> annotatedOr(p, StringArg.class,
        a -> Builders.methodInvocation(a.value().getBrigadierType()).setStatic(Classes.STRING_ARGUMENT_TYPE),
        Builders.methodInvocation(StringArgType.WORD.getBrigadierType()).setStatic(Classes.STRING_ARGUMENT_TYPE),
        Builders.methodInvocation("getString").setStatic(Classes.STRING_ARGUMENT_TYPE)
            .addParameter(CodeExpression.variable("ctx"))
            .addParameter(CodeExpression.string(name))
    ), "java.lang.String");
  }

  protected final void putFor(BiFunction<SourceVariable, String, BrigadierArgumentType> value, String... keys) {
    for (String key : keys) {
      conversionMap.put(key, value);
    }
  }

  protected final <T extends Annotation> BrigadierArgumentType annotatedOr(
      SourceVariable variable,
      Class<T> annotation,
      Function<T, AsExpression> withAnnotation,
      AsExpression withoutAnnotation,
      AsExpression retrieval
  ) {
    return variable.getAnnotationOptional(annotation)
        .map(annotated -> BrigadierArgumentType.of(withAnnotation.apply(annotated), retrieval))
        .orElseGet(() -> BrigadierArgumentType.of(withoutAnnotation, retrieval));
  }

  public final BrigadierArgumentType getAsArgumentType(SourceVariable parameter) throws ConversionException {
    final String argumentName = parameter.getName();
    final String type = parameter.getType().getFullyQualifiedAndTypedName();

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
