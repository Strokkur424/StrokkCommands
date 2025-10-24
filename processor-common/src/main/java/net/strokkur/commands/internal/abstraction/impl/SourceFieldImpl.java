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

import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceField;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;

public class SourceFieldImpl extends AbstractSourceVariableImpl<VariableElement> implements SourceField {
  private final SourceClass enclosed;

  public SourceFieldImpl(final ProcessingEnvironment environment, final VariableElement element, final SourceClass enclosed) {
    super(environment, element);
    this.enclosed = enclosed;
  }

  @Override
  public SourceClass getEnclosed() {
    return this.enclosed;
  }

  @Override
  public boolean isInitialized() {
    final Trees trees = Trees.instance(this.environment);
    final VariableTree fieldTree = (VariableTree) trees.getTree(element);
    return fieldTree.getInitializer() != null;
  }
}
