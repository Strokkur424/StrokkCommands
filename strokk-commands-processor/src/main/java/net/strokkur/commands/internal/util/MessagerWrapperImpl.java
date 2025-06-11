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
package net.strokkur.commands.internal.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

class MessagerWrapperImpl implements MessagerWrapper {
    private static final boolean OUTPUT_INFO = false;
    
    private final Messager messager;

    public MessagerWrapperImpl(Messager messager) {
        this.messager = messager;
    }

    @Override
    public void info(String format, Object... arguments) {
        if (OUTPUT_INFO) {
            messager.printNote(format.replaceAll("\\{}", "%s").formatted(arguments));
        }
    }

    @Override
    public void infoElement(String format, Element element, Object... arguments) {
        if (OUTPUT_INFO) {
            messager.printNote(format.replaceAll("\\{}", "%s").formatted(arguments), element);
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        messager.printError(format.replaceAll("\\{}", "%s").formatted(arguments));
    }

    @Override
    public void errorElement(String format, Element element, Object... arguments) {
        messager.printError(format.replaceAll("\\{}", "%s").formatted(arguments), element);
    }
}
