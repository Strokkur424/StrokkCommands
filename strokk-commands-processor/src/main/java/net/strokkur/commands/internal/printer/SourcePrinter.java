package net.strokkur.commands.internal.printer;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

public interface SourcePrinter {

    String INDENTATION = "\s\s\s\s";

    void print() throws IOException;

    @Nullable
    Writer getWriter();

    void setWriter(@Nullable Writer writer);

    void incrementIndent();

    void decrementIndent();

    void print(String message, Object... format) throws IOException;

    void println(String message, Object... format) throws IOException;

    void println() throws IOException;

    void printBlock(String block, Object... format) throws IOException;
}
