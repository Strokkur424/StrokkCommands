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
package net.strokkur.commands.internal.abstraction.impl;

import net.strokkur.commands.internal.abstraction.SourceRecord;
import net.strokkur.commands.internal.abstraction.SourceRecordComponent;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.DeclaredType;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SourceRecordImpl extends SourceClassImpl implements SourceRecord {
  private final List<SourceRecordComponent> recordComponents;

  public SourceRecordImpl(final ProcessingEnvironment environment, final DeclaredType type) {
    super(environment, type);
    this.recordComponents = new LinkedList<>();
    for (final Element enclosed : type.asElement().getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.RECORD_COMPONENT) {
        this.recordComponents.add(new SourceRecordComponentImpl(environment, (RecordComponentElement) enclosed));
      }
    }
  }

  @Override
  public List<SourceRecordComponent> getRecordComponents() {
    return Collections.unmodifiableList(this.recordComponents);
  }
}
