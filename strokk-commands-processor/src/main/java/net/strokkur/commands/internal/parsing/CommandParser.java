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

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.LiteralCommandPath;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface CommandParser {

  CommandPath<?> createCommandTree(TypeElement typeElement);

  void parseElement(CommandPath<?> path, Element element);

  void parseClass(CommandPath<?> path, TypeElement element);

  void parseMethod(CommandPath<?> path, ExecutableElement element);

  void parseField(CommandPath<?> path, VariableElement element);

  List<List<CommandArgument>> parseArguments(List<VariableElement> elements, TypeElement typeElement);

  @Nullable
  @SuppressWarnings("ConstantValue")
  default <A extends Annotation> LiteralCommandPath getLiteralPath(Element element, Class<A> annotation, Function<A, String> valueExtract) {
    final A a = element.getAnnotation(annotation);
    if (a == null) {
      return null;
    }

    final String path = valueExtract.apply(a);
    if (path == null || path.isBlank()) {
      return null;
    }

    final List<LiteralCommandArgument> list = new ArrayList<>();
    for (String lit : path.split(" ")) {
      list.add(LiteralCommandArgument.literal(lit, element));
    }

    return new LiteralCommandPath(list);
  }
}
