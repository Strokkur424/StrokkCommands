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
import net.strokkur.commands.internal.StrokkCommandsProcessor;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.util.ForwardingMessagerWrapper;
import net.strokkur.commands.internal.util.MessagerWrapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

class FieldTransform implements PathTransform, ForwardingMessagerWrapper {

  private final CommandParser parser;
  private final MessagerWrapper messager;

  public FieldTransform(final CommandParser parser, final MessagerWrapper messager) {
    this.parser = parser;
    this.messager = messager;
  }

  @Override
  public void transform(final CommandPath<?> parent, final Element element) {
    debug("> FieldTransform: {}.{}", element.getEnclosingElement().getSimpleName(), element.getSimpleName());
    final CommandPath<?> thisPath = createThisPath(parent, this.parser, element);

    thisPath.setAttribute(AttributeKey.ACCESS_STACK, new ArrayList<>(List.of(ExecuteAccess.of((VariableElement) element))));

    this.parser.hardParse(thisPath, StrokkCommandsProcessor.getTypes().asElement(element.asType()));
  }

  @Override
  public boolean hardRequirement(final Element element) {
    return element.getKind() == ElementKind.FIELD;
  }

  @Override
  public boolean weakRequirement(final Element element) {
    //noinspection ConstantValue
    return element.getAnnotation(Command.class) != null || element.getAnnotation(Subcommand.class) != null;
  }

  @Override
  public MessagerWrapper delegateMessager() {
    return this.messager;
  }
}
