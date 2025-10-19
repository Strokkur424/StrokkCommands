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

import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class ClassTransform implements PathTransform<TypeElement>, ForwardingMessagerWrapper {
  private static final Set<ElementKind> ENCLOSED_ELEMENTS_TO_PARSE = Set.of(
      ElementKind.METHOD,
      ElementKind.FIELD,
      ElementKind.CLASS,
      ElementKind.RECORD
  );

  protected final CommandParser parser;
  private final MessagerWrapper messager;

  public ClassTransform(final CommandParser parser, final MessagerWrapper messager) {
    this.parser = parser;
    this.messager = messager;
  }

  @Override
  public void transform(final CommandPath<?> parent, final TypeElement element) {
    debug("> ClassTransform: parsing {}...", element);

    final CommandPath<?> thisPath = this.createThisPath(parent, this.parser, element);
    addAccessAttribute(thisPath, element);

    final List<CommandPath<?>> relevant = parseRecordComponents(thisPath, element);

    for (final Element enclosed : element.getEnclosedElements()) {
      if (!ENCLOSED_ELEMENTS_TO_PARSE.contains(enclosed.getKind())) {
        continue;
      }

      for (final CommandPath<?> recordPath : relevant) {
        this.parser.parseElement(recordPath, enclosed);
      }
    }
  }

  protected void addAccessAttribute(final CommandPath<?> path, final TypeElement element) {
    final InstanceAccess access = ExecuteAccess.of(element);
    path.editAttributeMutable(
        AttributeKey.ACCESS_STACK,
        accesses -> accesses.add(access),
        () -> new ArrayList<>(List.of(access))
    );
  }

  protected List<CommandPath<?>> parseRecordComponents(final CommandPath<?> parent, final TypeElement element) {
    return Collections.singletonList(parent);
  }

  @Override
  public boolean requirement(final TypeElement element) {
    return element.getAnnotation(Command.class) != null || element.getAnnotation(Subcommand.class) != null;
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return this.messager;
  }
}
