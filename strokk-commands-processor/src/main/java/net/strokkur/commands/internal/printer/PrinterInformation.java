package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;

import java.util.Set;
import java.util.Stack;

interface PrinterInformation {

    CommandPath<?> getCommandPath();

    CommandInformation getCommandInformation();

    Set<String> getPrintedInstances();

    Stack<ExecuteAccess<?>> getAccessStack();
}
