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
package net.strokkur.commands.internal.intermediate.tree;

import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.LiteralCommandArgument;
import net.strokkur.commands.internal.arguments.MultiLiteralCommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.jap.code.documentation.DiscardingDocumentationRenderer;
import net.strokkur.jap.code.visitor.source.JavaSourcePrintingVisitor;

public class ArgumentNode extends AbstractCommandNode {
  private final CommandArgument argument;

  public ArgumentNode(CommandArgument argument) {
    this.argument = argument;
  }

  @Override
  public String name() {
    return switch (this.argument) {
      case MultiLiteralCommandArgument multi -> '[' + String.join("|", multi.literals()) + ']';
      case LiteralCommandArgument lit -> lit.literal();
      case RequiredCommandArgument req -> {
        final JavaSourcePrintingVisitor visitor = new JavaSourcePrintingVisitor(DiscardingDocumentationRenderer::new, "", "");
        final String rawInitializer = req.argumentType().initializer().toExpression().accept(visitor).toString();
        final String initializer = rawInitializer.replace("\n", " ");
        yield req.argumentName() + " (" + initializer + ')';
      }
      default -> throw new IllegalStateException("Unknown argument type class: " + this.argument.getClass());
    };
  }

  /// {@return the argument held by this node}
  public CommandArgument argument() {
    return this.argument;
  }
}
