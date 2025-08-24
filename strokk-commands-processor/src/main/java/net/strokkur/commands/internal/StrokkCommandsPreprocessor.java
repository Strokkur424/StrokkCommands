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
package net.strokkur.commands.internal;

import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.ExecutorType;
import net.strokkur.commands.internal.intermediate.attributes.AttributeKey;
import net.strokkur.commands.internal.intermediate.paths.CommandPath;
import net.strokkur.commands.internal.parsing.CommandParser;
import net.strokkur.commands.internal.parsing.CommandParserImpl;
import net.strokkur.commands.internal.printer.CommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.Set;

@NullMarked
public class StrokkCommandsPreprocessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Command.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_21;
    }

    @Override
    @NullUnmarked
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final MessagerWrapper messagerWrapper = MessagerWrapper.wrap(super.processingEnv.getMessager());
        final BrigadierArgumentConverter converter = new BrigadierArgumentConverter(messagerWrapper);
        final CommandParser parser = new CommandParserImpl(messagerWrapper, converter);

        for (Element element : roundEnv.getElementsAnnotatedWith(Command.class)) {
            if (!(element instanceof TypeElement typeElement) || typeElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                // Element is not a top-level class
                continue;
            }

            final CommandInformation commandInformation = getCommandInformation(typeElement);
            final CommandPath<?> commandPath = parser.parseElement(typeElement);

            if (System.getProperty(MessagerWrapper.DEBUG_SYSTEM_PROPERTY) != null) {
                // debug log all paths.
                messagerWrapper.debug(commandPath.toString());
            }

            // Before we print the paths, we do a step I like to refer to as "flattening".
            // This does not actually change the structure of the paths, but it moves up any attributes
            // relevant for certain things to print correctly (a.e. executor requirements).
            flattenPath(commandPath);

            try {
                final CommandTreePrinter printer = new CommandTreePrinter(0, null, commandPath, commandInformation);
                final JavaFileObject obj = processingEnv.getFiler().createSourceFile(printer.getPackageName() + "." + printer.getBrigadierClassName());

                try (PrintWriter out = new PrintWriter(obj.openWriter())) {
                    printer.setWriter(out);
                    printer.print();
                }
            } catch (Exception ex) {
                messagerWrapper.errorElement("A fatal exception occurred whilst printing source file: {}", typeElement, ex.getMessage());
                ex.printStackTrace();
            }
        }

        return true;
    }

    @NullUnmarked
    private CommandInformation getCommandInformation(@NonNull TypeElement typeElement) {
        Description description = typeElement.getAnnotation(Description.class);
        Aliases aliases = typeElement.getAnnotation(Aliases.class);

        return new CommandInformation(
            typeElement,
            description != null ? description.value() : null,
            aliases != null ? aliases.value() : null
        );
    }

    private void flattenPath(CommandPath<?> path) {
        // Depth first.
        path.getChildren().forEach(this::flattenPath);

        // The relevant attributes are the 'permission', 'requires_op', 'executor, and 'requirement'
        // attributes, since these add some sort of `.requires` clause, which works the best the higher
        // up it is in the tree. Certain attribute values also cause the parent value to be written to
        // explicitly in order to **avoid** merging values. A.e. a NONE executor will force itself to
        // the parent in order to avoid being  swallowed by a sister path, which may have an executor requirement.

        // Due to the way some attributes are ordered, certain attributes must be set from the parent, whilst
        // others are set from the children. Each attribute will have a short description of the order.

        // Once an attribute is passed on, it will be removed from the child node in order to help the
        // source file printer a bit.

        // REQUIRES_OP - This attribute is set from the parent. It is only set if **all children**
        // have this attribute set as well.
        if (!path.hasAttribute(AttributeKey.REQUIRES_OP)) {
            boolean mayRequireOp = true;
            boolean childRequiresOp = false;
            for (final CommandPath<?> child : path.getChildren()) {
                if (child.hasAttribute(AttributeKey.REQUIRES_OP)) {
                    childRequiresOp = true;
                } else {
                    mayRequireOp = false;
                    break;
                }
            }

            if (mayRequireOp && childRequiresOp) {
                path.setAttribute(AttributeKey.REQUIRES_OP, true);
                for (final CommandPath<?> child : path.getChildren()) {
                    child.removeAttribute(AttributeKey.REQUIRES_OP);
                }
            }
        }

        // EXECUTOR - Here, the case is similar to the REQUIRES_OP attribute, which the slight difference that
        // the **minimum requirement** will be used, meaning if a child has an ENTITY, and another a PLAYER
        // executor requirement, the parent path will declare an ENTITY executor requirement as well.
        {
            boolean nodesHaveExecutorRequirement = false;
            ExecutorType relevantExecutorType = ExecutorType.PLAYER;
            for (final CommandPath<?> child : path.getChildren()) {
                if (!child.hasAttribute(AttributeKey.EXECUTOR_TYPE)) {
                    // If a child node doesn't have a req, neither will the parent node.
                    relevantExecutorType = ExecutorType.NONE;
                    break;
                }

                nodesHaveExecutorRequirement = true;
                final ExecutorType executorType = child.getAttribute(AttributeKey.EXECUTOR_TYPE);
                if (relevantExecutorType == ExecutorType.PLAYER && executorType == ExecutorType.ENTITY) {
                    relevantExecutorType = ExecutorType.ENTITY;
                }

                if (executorType == ExecutorType.NONE) {
                    relevantExecutorType = ExecutorType.NONE;
                    break;
                }
            }

            if (relevantExecutorType != ExecutorType.NONE && nodesHaveExecutorRequirement) {
                path.setAttribute(AttributeKey.EXECUTOR_TYPE, relevantExecutorType);
                for (final CommandPath<?> child : path.getChildren()) {
                    // Because the executor attribute works a bit differently than just a normal requirement,
                    // the attribute itself cannot be removed. Instead, we can set the EXECUTOR_HANDLED
                    // attribute to tell the printer to **not** print the requirement.
                    child.setAttribute(AttributeKey.EXECUTOR_HANDLED, true);
                }
            }
        }

        // PERMISSION - not implemented

        // REQUIREMENT - not implemented
    }
}