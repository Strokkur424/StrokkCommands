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

import net.strokkur.commands.arguments.StringArgType;
import net.strokkur.commands.arguments.DoubleArg;
import net.strokkur.commands.arguments.FloatArg;
import net.strokkur.commands.arguments.IntArg;
import net.strokkur.commands.arguments.LongArg;
import net.strokkur.commands.arguments.StringArg;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.exceptions.ConversionException;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class BrigadierArgumentConverter implements ForwardingMessagerWrapper {
  private final MessagerWrapper messagerWrapper;
  protected final Map<String, BiFunction<SourceVariable, String, BrigadierArgumentType>> conversionMap;

  public BrigadierArgumentConverter(final MessagerWrapper messagerWrapper) {
    this.messagerWrapper = messagerWrapper;
    this.conversionMap = new TreeMap<>();
    initializeArguments();
  }

  protected abstract @Nullable BrigadierArgumentType handleCustomArgumentAnnotations(
      String argumentName,
      String type,
      SourceVariable parameter
  ) throws ConversionException;

  protected void initializeArguments() {
    putFor((unused, name) -> BrigadierArgumentType.of(
        "BoolArgumentType.bool()",
        "BoolArgumentType.getBool(ctx, \"%s\")".formatted(name),
        "com.mojang.brigadier.arguments.BoolArgumentType"
    ), "boolean", "java.lang.Boolean");

    putFor((p, name) -> annotatedOr(p, IntArg.class,
        a -> "IntegerArgumentType.integer(%s, %s)".formatted(a.min(), a.max()),
        "IntegerArgumentType.integer()", "IntegerArgumentType.getInteger(ctx, \"%s\")".formatted(name),
        "com.mojang.brigadier.arguments.IntegerArgumentType"
    ), "int", "java.lang.Integer");

    putFor((p, name) -> annotatedOr(p, LongArg.class,
        a -> "LongArgumentType.longArg(%s, %s)".formatted(a.min(), a.max()),
        "LongArgumentType.longArg()", "LongArgumentType.getLong(ctx, \"%s\")".formatted(name),
        "com.mojang.brigadier.arguments.LongArgumentType"
    ), "long", "java.lang.Long");

    putFor((p, name) -> annotatedOr(p, FloatArg.class,
        a -> "FloatArgumentType.floatArg(%s, %s)".formatted(a.min(), a.max()),
        "FloatArgumentType.floatArg()",
        "FloatArgumentType.getFloat(ctx, \"%s\")".formatted(name),
        "com.mojang.brigadier.arguments.FloatArgumentType"
    ), "float", "java.lang.Float");

    putFor((p, name) -> annotatedOr(p, DoubleArg.class,
        a -> "DoubleArgumentType.doubleArg(%s, %s)".formatted(a.min(), a.max()),
        "DoubleArgumentType.doubleArg()", "DoubleArgumentType.getDouble(ctx, \"%s\")".formatted(name),
        "com.mojang.brigadier.arguments.DoubleArgumentType"
    ), "double", "java.lang.Double");

    putFor((p, name) -> annotatedOr(p, StringArg.class,
        a -> "StringArgumentType.%s()".formatted(a.value().getBrigadierType()),
        "StringArgumentType.%s()".formatted(StringArgType.WORD.getBrigadierType()),
        "StringArgumentType.getString(ctx, \"%s\")".formatted(name),
        "com.mojang.brigadier.arguments.StringArgumentType"
    ), "java.lang.String");
  }

  protected final void putFor(final BiFunction<SourceVariable, String, BrigadierArgumentType> value, final String... keys) {
    for (String key : keys) {
      conversionMap.put(key, value);
    }
  }

  protected final <T extends Annotation> BrigadierArgumentType annotatedOr(
      final SourceVariable variable,
      final Class<T> annotation,
      final Function<T, String> withAnnotation,
      final String withoutAnnotation,
      final String retrieval,
      final String singleImport
  ) {
    return annotatedOr(variable, annotation, withAnnotation, withoutAnnotation, retrieval, Set.of(singleImport));
  }

  protected final <T extends Annotation> BrigadierArgumentType annotatedOr(
      final SourceVariable variable,
      final Class<T> annotation,
      final Function<T, String> withAnnotation,
      final String withoutAnnotation,
      final String retrieval,
      final Set<String> imports
  ) {
    return variable.getAnnotationOptional(annotation)
        .map(annotated -> BrigadierArgumentType.of(withAnnotation.apply(annotated), retrieval, imports))
        .orElseGet(() -> BrigadierArgumentType.of(withoutAnnotation, retrieval, imports));
  }

  public final BrigadierArgumentType getAsArgumentType(final SourceVariable parameter) throws ConversionException {
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
