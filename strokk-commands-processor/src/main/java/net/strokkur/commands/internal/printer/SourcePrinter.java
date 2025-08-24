package net.strokkur.commands.internal.printer;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

public interface SourcePrinter {

    String INDENTATION = "\s\s\s\s";

    void print() throws IOException;

    void setWriter(@Nullable Writer writer);
}
