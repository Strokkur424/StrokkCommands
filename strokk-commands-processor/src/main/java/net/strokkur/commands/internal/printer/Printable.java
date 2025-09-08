package net.strokkur.commands.internal.printer;

import java.io.IOException;

interface Printable {

    void incrementIndent();

    void decrementIndent();

    void print(String message, Object... format) throws IOException;

    void printIndent() throws IOException;

    void println(String message, Object... format) throws IOException;

    void println() throws IOException;

    void printBlock(String block, Object... format) throws IOException;
}
