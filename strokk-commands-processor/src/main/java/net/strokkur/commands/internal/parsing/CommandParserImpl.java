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

import net.strokkur.commands.annotations.Literal;
import net.strokkur.commands.annotations.Suggestion;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgumentImpl;
import net.strokkur.commands.internal.exceptions.HandledConversionException;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.EmptyCommandPath;
import net.strokkur.commands.internal.intermediate.suggestions.SuggestionProvider;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.util.Utils;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class CommandParserImpl implements CommandParser, ForwardingMessagerWrapper {
  private final PathTransform<TypeElement> classTransform;
  private final PathTransform<TypeElement> recordTransform;
  private final PathTransform<ExecutableElement> methodTransform;
  private final PathTransform<VariableElement> fieldTransform;

  private final MessagerWrapper messager;
  private final BrigadierArgumentConverter converter;

  public CommandParserImpl(final MessagerWrapper messager, final BrigadierArgumentConverter converter) {
    this.messager = messager;
    this.converter = converter;

    this.classTransform = new ClassTransform(this, messager);
    this.recordTransform = new RecordTransform(this, messager);
    this.methodTransform = new MethodTransform(this, messager);
    this.fieldTransform = new FieldTransform(this, messager);
  }

  @Override
  public CommandPath<?> createCommandTree(final TypeElement typeElement) {
    final CommandPath<?> empty = new EmptyCommandPath();
    parseElement(empty, typeElement);
    return empty.getChildren().getFirst();
  }

  @Override
  public void parseElement(final CommandPath<?> path, final Element element) {
    switch (element) {
      case TypeElement type -> {
        if (type.getKind() == ElementKind.RECORD) {
          this.recordTransform.transformIfRequirement(path, type);
        } else {
          this.classTransform.transformIfRequirement(path, type);
        }
      }
      case ExecutableElement method -> this.methodTransform.transformIfRequirement(path, method);
      case VariableElement var -> this.fieldTransform.transformIfRequirement(path, var);
      default -> {
      }
    }
  }

  @Override
  public void parseClass(final CommandPath<?> path, final TypeElement element) {
    if (element.getKind() == ElementKind.RECORD) {
      this.recordTransform.transform(path, element);
    } else if (element.getKind() == ElementKind.CLASS) {
      this.classTransform.transform(path, element);
    } else {
      throw new IllegalStateException("Unknown class type: " + element.getKind().name());
    }
  }

  @Override
  public void parseMethod(final CommandPath<?> path, final ExecutableElement element) {
    this.methodTransform.transform(path, element);
  }

  @Override
  public void parseField(final CommandPath<?> path, final VariableElement element) {
    if (element.getKind() == ElementKind.FIELD) {
      this.fieldTransform.transform(path, element);
    } else {
      this.infoElement("Tried to parse variable elements as field", element);
    }
  }

  @Override
  public List<List<CommandArgument>> parseArguments(final List<VariableElement> elements, final TypeElement typeElement) {
    final List<List<CommandArgument>> arguments = new ArrayList<>();
    arguments.add(new ArrayList<>(elements.size()));

    for (final VariableElement parameter : elements) {
      debug("| Parsing parameter: " + parameter.getSimpleName());

      final Literal literal = parameter.getAnnotation(Literal.class);
      if (literal != null) {
        final String[] declared = literal.value();
        if (declared.length == 0) {
          arguments.forEach(argumentList -> argumentList.add(LiteralCommandArgument.literal(parameter.getSimpleName().toString(), parameter)));
        } else if (declared.length == 1) {
          arguments.forEach(argumentList -> argumentList.add(LiteralCommandArgument.literal(declared[0], parameter)));
        } else {
          // This is a worst-case scenario. All nested lists need to be duplicated as many times as there are literals, with each
          // list being added a different literal.
          final List<List<CommandArgument>> empty = new ArrayList<>();
          for (final String lit : declared) {
            for (final List<CommandArgument> argument : arguments) {
              final List<CommandArgument> clone = new ArrayList<>(argument);
              clone.add(LiteralCommandArgument.literal(lit, parameter));
              empty.add(clone);
            }
          }

          arguments.clear();
          arguments.addAll(empty);
        }
        continue;
      }

      final BrigadierArgumentType argumentType;
      try {
        argumentType = converter.getAsArgumentType(parameter);
      } catch (HandledConversionException e) {
        debug("  | Due to an handled exception, the parameter parsing has been cancelled.");
        continue;
      }

      debug("  | Successfully found Brigadier type: {}", argumentType);

      final SuggestionProvider suggestionProvider = getSuggestionProvider(typeElement, parameter);
      if (suggestionProvider != null) {
        debug("  | Suggestion provider: {}", suggestionProvider);
      }

      final String name = parameter.getSimpleName().toString();
      for (final List<CommandArgument> argument : arguments) {
        argument.add(new RequiredCommandArgumentImpl(argumentType, name, parameter, suggestionProvider));
      }
    }

    return arguments;
  }

  @Nullable
  private SuggestionProvider getSuggestionProvider(final TypeElement classElement, final VariableElement parameter) {
    final Suggestion suggestion = parameter.getAnnotation(Suggestion.class);
    if (suggestion == null) {
      return null;
    }

    final TypeMirror baseClass;
    try {
      final TypeMirror base = Utils.getAnnotationMirror(parameter, Suggestion.class, "base");

      if (base == null) {
        baseClass = classElement.asType();
      } else {
        baseClass = base;
      }
    } catch (Exception ex) {
      //noinspection CallToPrintStackTrace
      ex.printStackTrace();
      throw ex;
    }

    if (suggestion.method().isBlank() && suggestion.field().isBlank()) {
      if (baseClass == null) {
        infoElement("@Suggestion annotation was used, but no parameters were passed.", parameter);
        return null;
      }

      return SuggestionProvider.ofClass(baseClass);
    }

    if (!suggestion.method().isBlank()) {
      if (suggestion.reference()) {
        return SuggestionProvider.ofMethodReference(baseClass, suggestion.method());
      }
      return SuggestionProvider.ofMethod(baseClass, suggestion.method());
    }

    if (!suggestion.field().isBlank()) {
      return SuggestionProvider.ofField(baseClass, suggestion.field());
    }

    errorElement("Internal exception: Suggestion annotation is not null, but no provider was found. Please report this at https://discord.strokkur.net.", parameter);
    return null;
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return messager;
  }
}
