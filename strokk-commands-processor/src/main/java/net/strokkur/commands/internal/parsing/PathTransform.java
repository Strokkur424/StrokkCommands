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
import net.strokkur.commands.annotations.Executes;
import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.RequiresOP;
import net.strokkur.commands.annotations.Subcommand;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.EmptyCommandPath;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import java.util.Set;

interface PathTransform {

    void transform(CommandPath<?> parent, Element element);

    boolean hardRequirement(Element element);

    boolean weakRequirement(Element element);

    default boolean shouldTransform(Element element) {
        return hardRequirement(element) && weakRequirement(element);
    }

    default CommandPath<?> createThisPath(CommandPath<?> parent, CommandParser parser, Element element) {
        CommandPath<?> thisPath = parser.getLiteralPath(element, Command.class, Command::value);
        if (thisPath == null) {
            thisPath = parser.getLiteralPath(element, Subcommand.class, Subcommand::value);
        }
        return populatePath(parent, thisPath, element);
    }

    default CommandPath<?> createThisExecutesPath(CommandPath<?> parent, CommandParser parser, Element element) {
        CommandPath<?> thisPath = parser.getLiteralPath(element, Executes.class, Executes::value);
        return populatePath(parent, thisPath, element);
    }

    private CommandPath<?> populatePath(CommandPath<?> parent, @Nullable CommandPath<?> thisPath, Element element) {
        if (thisPath == null) {
            thisPath = new EmptyCommandPath();
        }

        // Add permission and RequiresOP clauses
        final Permission permission = element.getAnnotation(Permission.class);
        if (permission != null) {
            thisPath.setAttribute(AttributeKey.PERMISSIONS, Set.of(permission.value()));
        }

        if (element.getAnnotation(RequiresOP.class) != null) {
            thisPath.setAttribute(AttributeKey.REQUIRES_OP, true);
        }

        parent.addChild(thisPath);
        return thisPath;
    }
}
