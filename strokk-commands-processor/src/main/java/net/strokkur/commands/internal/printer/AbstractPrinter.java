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
package net.strokkur.commands.internal.printer;

import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractPrinter implements Printable {

  private final String indentPreset = "\s\s\s\s";
  private @Nullable Writer writer;
  private String indentString;
  private int indent;

  public AbstractPrinter(int indent, @Nullable Writer writer) {
    this.writer = writer;
    this.indent = indent;
    this.indentString = indentPreset.repeat(this.indent);
  }

  public void setWriter(@Nullable Writer writer) {
    this.writer = writer;
  }

  @Override
  public void incrementIndent() {
    this.indent++;
    this.indentString = indentPreset.repeat(this.indent);
  }

  @Override
  public void decrementIndent() {
    if (indent == 0) {
      return;
    }

    this.indent--;
    this.indentString = indentPreset.repeat(this.indent);
  }

  @Override
  public void print(String message, Object... format) throws IOException {
    if (writer == null) {
      throw new IOException("No writer set.");
    }

    writer.append(message.replace("{}", "%s").formatted(format));
  }

  @Override
  public void printIndent() throws IOException {
    if (writer == null) {
      throw new IOException("No writer set.");
    }

    writer.append(indentString);
  }

  @Override
  public void println(String message, Object... format) throws IOException {
    if (writer == null) {
      throw new IOException("No writer set.");
    }

    message = message.stripTrailing();
    if (!message.isBlank()) {
      writer.append(indentString);
      writer.append(message.replace("{}", "%s").formatted(format));
    }
    writer.append("\n");
  }

  @Override
  public void println() throws IOException {
    if (writer == null) {
      throw new IOException("No writer set.");
    }

    writer.append("\n");
  }

  @Override
  public void printBlock(String block, Object... format) throws IOException {
    if (writer == null) {
      throw new IOException("No writer set.");
    }

    String parsedBlock = block.replace("{}", "%s").formatted(format);
    for (@Language("JAVA") String line : parsedBlock.split("\n")) {
      println(line);
    }
  }
}