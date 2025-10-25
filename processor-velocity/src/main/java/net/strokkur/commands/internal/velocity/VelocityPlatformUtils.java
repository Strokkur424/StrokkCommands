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
package net.strokkur.commands.internal.velocity;

import net.strokkur.commands.internal.PlatformUtils;
import net.strokkur.commands.internal.abstraction.AnnotationsHolder;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.SourceVariable;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.arguments.BrigadierArgumentType;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgumentImpl;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.util.MessagerWrapper;
import net.strokkur.commands.internal.velocity.util.VelocityAttributeKeys;
import net.strokkur.commands.velocity.Permission;

import java.util.Set;

final class VelocityPlatformUtils extends PlatformUtils {
  public VelocityPlatformUtils(final MessagerWrapper messager, final BrigadierArgumentConverter converter) {
    super(messager, converter);
  }

  @Override
  protected RequiredCommandArgument constructRequiredCommandArgument(
      final BrigadierArgumentType type,
      final String name,
      final SourceVariable parameter,
      final SourceClass source
  ) {
    return new RequiredCommandArgumentImpl(type, name, parameter);
  }

  @Override
  public void populateNode(final CommandNode node, final AnnotationsHolder element) {
    element.getAnnotationOptional(Permission.class).ifPresent(
        permission -> node.editAttributeMutable(VelocityAttributeKeys.PERMISSIONS, s -> s.add(permission.value()), () -> Set.of(permission.value()))
    );
  }
}
