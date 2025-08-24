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
}