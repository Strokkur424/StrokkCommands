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

import net.strokkur.commands.internal.abstraction.SourceElement;
import net.strokkur.commands.internal.abstraction.impl.ElementGettable;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

public interface MessagerWrapper {

  String DEBUG_SYSTEM_PROPERTY = "strokk.commands.debug";
  String DEBUG_ONLY_SYSTEM_PROPERTY = "strokk.commands.debug.only";

  static MessagerWrapper wrap(Messager messager) {
    return new MessagerWrapperImpl(messager);
  }

  void print(Kind kind, String format, Object... arguments);

  void printElement(Kind kind, String format, Element element, Object... arguments);

  default void printSource(Kind kind, String format, SourceElement element, Object... arguments) {
    if (element instanceof ElementGettable<?> defaultImpl) {
      printElement(kind, format, defaultImpl.getElement(), arguments);
    } else {
      print(kind, format, arguments);
    }
  }

  /**
   * Prints a formatted message to the {@link Kind#OTHER} channel.
   * <p>
   * The message is silently discarded unless the system property {@code -Dstrokk.command.debug} is set.
   */
  default void debug(String format, Object... args) {
    if (System.getProperty(DEBUG_SYSTEM_PROPERTY) != null) {
      print(Kind.OTHER, format, args);
    }
  }

  /**
   * Prints a formatted message about this element to the {@link Kind#OTHER} channel.
   * <p>
   * The message is silently discarded unless the system property {@code -Dstrokk.command.debug} is set.
   */
  default void debugElement(String format, Element element, Object... args) {
    if (System.getProperty(DEBUG_SYSTEM_PROPERTY) != null) {
      printElement(Kind.OTHER, format, element, args);
    }
  }

  /**
   * Prints a formatted message about this element to the {@link Kind#OTHER} channel.
   * <p>
   * The message is silently discarded unless the system property {@code -Dstrokk.command.debug} is set.
   */
  default void debugSource(String format, SourceElement element, Object... args) {
    if (System.getProperty(DEBUG_SYSTEM_PROPERTY) != null) {
      printSource(Kind.OTHER, format, element, args);
    }
  }

  /**
   * Prints a formatted message to the {@link Kind#NOTE} channel.
   */
  default void info(String format, Object... arguments) {
    print(Kind.NOTE, format, arguments);
  }

  /**
   * Prints a formatted message about this element to the {@link Kind#NOTE} channel.
   */
  default void infoElement(String format, Element element, Object... arguments) {
    printElement(Kind.NOTE, format, element, arguments);
  }

  /**
   * Prints a formatted message about this element to the {@link Kind#NOTE} channel.
   */
  default void infoElement(String format, SourceElement element, Object... arguments) {
    printSource(Kind.NOTE, format, element, arguments);
  }

  /**
   * Prints a formatted message to the {@link Kind#ERROR} channel.
   */
  default void error(String format, Object... arguments) {
    print(Kind.ERROR, format, arguments);
  }

  /**
   * Prints a formatted message about this element to the {@link Kind#ERROR} channel.
   */
  default void errorElement(String format, Element element, Object... arguments) {
    printElement(Kind.ERROR, format, element, arguments);
  }

  /**
   * Prints a formatted message about this element to the {@link Kind#ERROR} channel.
   */
  default void errorSource(String format, SourceElement element, Object... arguments) {
    printSource(Kind.ERROR, format, element, arguments);
  }
}
