package net.strokkur.commands.internal.printer;

import net.strokkur.commands.internal.StrokkCommandsPreprocessor;
import net.strokkur.commands.internal.arguments.CommandArgument;
import net.strokkur.commands.internal.arguments.RequiredCommandArgument;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.access.ExecuteAccess;
import net.strokkur.commands.internal.intermediate.access.FieldAccess;
import net.strokkur.commands.internal.intermediate.access.InstanceAccess;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.intermediate.paths.LiteralCommandPath;
import net.strokkur.commands.internal.util.Classes;
import net.strokkur.commands.internal.util.Utils;

import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

interface ImportPrinter extends Printable, PrinterInformation {

    Set<String> STANDARD_IMPORTS = Set.of(
        Classes.COMMAND,
        Classes.LITERAL_COMMAND_NODE,
        Classes.COMMAND_SOURCE_STACK,
        Classes.COMMANDS,
        Classes.LIST,
        Classes.NULL_MARKED
    );

    default void printImports(Set<String> imports) throws IOException {
        final Map<Boolean, List<String>> splitImports = imports.stream()
            .sorted()
            .collect(Collectors.partitioningBy(str -> str.startsWith("java")));

        final List<String> javaImports = splitImports.get(true);
        final List<String> otherImports = splitImports.get(false);

        for (String i : otherImports) {
            println("import {};", i);
        }

        println();

        for (String i : javaImports) {
            println("import {};", i);
        }
    }

    default Set<String> getImports() {
        final Set<String> imports = new HashSet<>(STANDARD_IMPORTS);
        gatherImports(imports, getCommandPath());

        imports.removeIf(importString -> {
            if (importString.startsWith("java.lang")) {
                return true;
            }

            final TypeElement element = StrokkCommandsPreprocessor.getElements().getTypeElement(importString);
            if (element == null) {
                return false;
            }

            return Utils.getPackageElement(element) == Utils.getPackageElement(getCommandInformation().classElement());
        });

        return imports;
    }

    private void gatherImports(Set<String> imports, CommandPath<?> commandPath) {
        if (commandPath.hasAttribute(AttributeKey.ACCESS_STACK)) {
            for (final ExecuteAccess<?> access : commandPath.getAttributeNotNull(AttributeKey.ACCESS_STACK)) {
                if (access instanceof InstanceAccess instanceAccess) {
                    imports.add(instanceAccess.getElement().getQualifiedName().toString());
                } else if (access instanceof FieldAccess fieldAccess) {
                    imports.add(((TypeElement) StrokkCommandsPreprocessor.getTypes().asElement(fieldAccess.getElement().asType())).getQualifiedName().toString());
                }
            }
        }

        if (!(commandPath instanceof LiteralCommandPath)) {
            for (final CommandArgument arg : commandPath.getArguments()) {
                if (arg instanceof RequiredCommandArgument requiredArgument) {
                    imports.addAll(requiredArgument.getArgumentType().imports());

                    if (requiredArgument.getSuggestionProvider() != null) {
                        final TypeElement suggestionsTypeElement = requiredArgument.getSuggestionProvider().getClassElement();
                        if (suggestionsTypeElement != null) {
                            imports.add(suggestionsTypeElement.getQualifiedName().toString());
                        }
                    }
                }
            }
        }

        final ExecutorType executorType = commandPath.getAttributeNotNull(AttributeKey.EXECUTOR_TYPE);
        if (executorType == ExecutorType.PLAYER) {
            imports.add(Classes.PLAYER);
        } else if (executorType == ExecutorType.ENTITY) {
            imports.add(Classes.ENTITY);
        }

        if (executorType != ExecutorType.NONE) {
            imports.add(Classes.SIMPLE_COMMAND_EXCEPTION_TYPE);
            imports.add(Classes.MESSAGE_COMPONENT_SERIALIZER);
            imports.add(Classes.COMPONENT);
        }

        for (final CommandPath<?> child : commandPath.getChildren()) {
            gatherImports(imports, child);
        }
    }
}
