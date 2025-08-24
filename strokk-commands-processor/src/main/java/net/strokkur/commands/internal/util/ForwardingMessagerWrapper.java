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

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public interface ForwardingMessagerWrapper extends MessagerWrapper {

    /**
     * {@return the messager wrapper to delegate all logger calls to}
     */
    MessagerWrapper delegateMessager();

    /**
     * {@inheritDoc}
     */
    @Override
    default void print(Diagnostic.Kind kind, String format, Object... arguments) {
        delegateMessager().print(kind, format, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void printElement(Diagnostic.Kind kind, String format, Element element, Object... arguments) {
        delegateMessager().printElement(kind, format, element, arguments);
    }
}
