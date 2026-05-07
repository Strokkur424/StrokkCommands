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

import net.strokkur.commands.internal.abstraction.SourceParameter;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.printer.CommonInstanceFieldPrinter;
import net.strokkur.commands.internal.velocity.util.VelocityClasses;

final class VelocityInstanceFieldPrinter extends CommonInstanceFieldPrinter {
  VelocityInstanceFieldPrinter(CommonCommandTreePrinter<?> printer) {
    super(printer);
  }

  @Override
  public String getParameterName(SourceParameter parameter) {
    if (parameter.getType().getFullyQualifiedName().equals(VelocityClasses.PROXY_SERVER)) {
      return "server";
    }
    return super.getParameterName(parameter);
  }
}
