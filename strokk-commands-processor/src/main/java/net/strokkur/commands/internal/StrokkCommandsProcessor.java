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

import com.sun.source.util.Trees;
import net.strokkur.commands.annotations.Aliases;
import net.strokkur.commands.annotations.Command;
import net.strokkur.commands.annotations.Description;
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.intermediate.CommandInformation;
import net.strokkur.commands.internal.intermediate.TreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.parsing.CommandParser;
import net.strokkur.commands.internal.parsing.CommandParserImpl;
import net.strokkur.commands.internal.printer.CommandTreePrinter;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Set;

public class StrokkCommandsProcessor extends AbstractProcessor {

  private static @Nullable Types types = null;
  private static @Nullable Elements elements = null;
  private static @Nullable Trees trees = null;

  public static Types getTypes() {
    return Objects.requireNonNull(types, "types is null");
  }

  public static Elements getElements() {
    return Objects.requireNonNull(elements, "elements is null");
  }

  public static Trees getTrees() {
    return Objects.requireNonNull(trees, "trees is null");
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(Command.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  @NullUnmarked
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    types = processingEnv.getTypeUtils();
    elements = processingEnv.getElementUtils();
    trees = Trees.instance(processingEnv);

    final MessagerWrapper messagerWrapper = MessagerWrapper.wrap(super.processingEnv.getMessager());
    final BrigadierArgumentConverter converter = new BrigadierArgumentConverter(messagerWrapper);
    final CommandParser parser = new CommandParserImpl(messagerWrapper, converter);
    final TreePostProcessor treePostProcessor = new TreePostProcessor(messagerWrapper);

    final String debugOnly = System.getProperty(MessagerWrapper.DEBUG_ONLY_SYSTEM_PROPERTY);
    if (debugOnly != null) {
      System.setProperty(MessagerWrapper.DEBUG_SYSTEM_PROPERTY, "true");
    }

    for (Element element : roundEnv.getElementsAnnotatedWith(Command.class)) {
      if (!(element instanceof TypeElement typeElement) || typeElement.getNestingKind().isNested()) {
        // Element is not a top-level class
        continue;
      }

      if (debugOnly != null && !typeElement.getQualifiedName().toString().contains(debugOnly)) {
        continue;
      }

      try {
        processElement(typeElement, messagerWrapper, parser, treePostProcessor);
      } catch (Exception e) {
        messagerWrapper.errorElement("An error occurred: {}", typeElement, e.getMessage());
        e.printStackTrace(new PrintWriter(System.out));
      }

      if (debugOnly != null) {
        break;
      }
    }

    types = null;
    elements = null;
    return true;
  }

  private void processElement(TypeElement typeElement, MessagerWrapper messagerWrapper, CommandParser parser, TreePostProcessor treePostProcessor) {
    boolean debug = System.getProperty(MessagerWrapper.DEBUG_SYSTEM_PROPERTY) != null;

    final CommandInformation commandInformation = getCommandInformation(typeElement);
    final CommandNode commandTree = parser.createCommandTree(typeElement.getAnnotation(Command.class).value(), typeElement);

    if (debug) {
      // debug log all paths.
      messagerWrapper.debug("Before flatten: \n{}\n ", commandTree.toString());
    }

    // Before we print the paths we do some post-processing to move some stuff around, which
    // is relevant for certain things to print correctly (a.e. executor requirements).
    treePostProcessor.cleanupPath(commandTree);
    treePostProcessor.applyDefaultExecutorPaths(commandTree);

    if (debug) {
      // debug log all paths.
      messagerWrapper.debug("After flatten: \n{}\n ", commandTree.toString());
    }

    try {
      final CommandTreePrinter printer = new CommandTreePrinter(0, null, commandTree, commandInformation);
      final JavaFileObject obj = processingEnv.getFiler().createSourceFile(printer.getPackageName() + "." + printer.getBrigadierClassName());

      try (PrintWriter out = new PrintWriter(obj.openWriter())) {
        printer.setWriter(out);
        printer.print();
      }
    } catch (Exception ex) {
      messagerWrapper.errorElement("A fatal exception occurred whilst printing source file: {}", typeElement, ex.getMessage());
      ex.printStackTrace(new PrintWriter(System.out));
    }
  }

  @NullUnmarked
  private CommandInformation getCommandInformation(@NonNull TypeElement typeElement) {
    final Description description = typeElement.getAnnotation(Description.class);
    final Aliases aliases = typeElement.getAnnotation(Aliases.class);

    ExecutableElement constructor = null;
    if (typeElement.getKind() == ElementKind.CLASS) {
      for (final Element enclosedElement : typeElement.getEnclosedElements()) {
        if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
          constructor = (ExecutableElement) enclosedElement;
          break;
        }
      }
    }

    return new CommandInformation(
        typeElement,
        constructor,
        description != null ? description.value() : null,
        aliases != null ? aliases.value() : null
    );
  }
}
