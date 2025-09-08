package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.ExecutablePath;
import net.strokkur.commands.internal.util.Utils;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.List;

interface InstanceFieldPrinter extends Printable, PrinterInformation {

    default void printAccessInstance(List<ExecuteAccess<?>> accesses) throws IOException {
        if (accesses.isEmpty()) {
            return; // IDK how this even happens, but what the hell am I supposed to do if the access stack is empty?
        }

        if (accesses.size() == 1) {
            if (getPrintedInstances().contains("instance")) {
                return;
            }
            final String typeName = accesses.getFirst().getTypeName();
            println("final {} instance = new {}();",
                typeName,
                typeName
            );
            getPrintedInstances().add("instance");
            return;
        }

        final ExecuteAccess<?> currentAccess = accesses.getLast();

        final String typeName = currentAccess.getTypeName();
        final String instanceName = Utils.getInstanceName(accesses);
        final String prevInstanceName = Utils.getInstanceName(accesses.subList(0, accesses.size() - 1));

        if (currentAccess instanceof FieldAccess fieldAccess) {
            final VariableElement fieldElement = fieldAccess.getElement();

            if (!getPrintedInstances().contains(prevInstanceName)) {
                printAccessInstance(accesses.subList(0, accesses.size() - 1));
            }

            if (Utils.isFieldInitialized(fieldElement)) {
                println("final {} {} = {}.{};",
                    typeName,
                    instanceName,
                    prevInstanceName,
                    fieldAccess.getElement().getSimpleName()
                );
            } else {
                println("final {} {} = new {}();",
                    typeName,
                    instanceName,
                    typeName
                );
            }

            getPrintedInstances().add(instanceName);
            return;
        }

        if (currentAccess instanceof InstanceAccess instanceAccess) {
            final TypeElement classElement = instanceAccess.getElement();
            if (classElement.getNestingKind() == NestingKind.TOP_LEVEL || classElement.getModifiers().contains(Modifier.STATIC)) {
                println("final {} {} = new {}();",
                    typeName,
                    instanceName,
                    typeName
                );
                getPrintedInstances().add(instanceName);
                return;
            }

            if (!getPrintedInstances().contains(prevInstanceName)) {
                printAccessInstance(accesses.subList(0, accesses.size() - 1));
            }

            println("final {} {} = {}.new {}();",
                typeName,
                instanceName,
                prevInstanceName,
                classElement.getSimpleName().toString()
            );
            getPrintedInstances().add(instanceName);
            return;
        }

        throw new IllegalStateException("Unknown access: " + currentAccess);
    }

    default void printInstanceFields(CommandPath<?> commandPath) throws IOException {
        int pushed = 0;
        if (commandPath.hasAttribute(AttributeKey.ACCESS_STACK)) {
            for (ExecuteAccess<?> executeAccess : commandPath.getAttributeNotNull(AttributeKey.ACCESS_STACK)) {
                if (executeAccess.getElement().getKind() == ElementKind.RECORD) {
                    for (int i = 0; i < pushed; i++) {
                        getAccessStack().pop();
                    }
                    return;
                }

                getAccessStack().push(executeAccess);
                pushed++;
            }
        }

        if (commandPath instanceof ExecutablePath) {
            printAccessInstance(getAccessStack());
        } else {
            for (final CommandPath<?> child : commandPath.getChildren()) {
                printInstanceFields(child);
            }
        }

        for (int i = 0; i < pushed; i++) {
            getAccessStack().pop();
        }
    }
}
