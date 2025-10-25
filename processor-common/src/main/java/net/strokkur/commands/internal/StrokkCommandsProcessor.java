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

import net.strokkur.commands.Command;
import net.strokkur.commands.internal.abstraction.SourceClass;
import net.strokkur.commands.internal.abstraction.impl.SourceClassImpl;
import net.strokkur.commands.internal.abstraction.impl.SourceRecordImpl;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.parsing.CommandParser;
import net.strokkur.commands.internal.parsing.CommandParserImpl;
import net.strokkur.commands.internal.parsing.DefaultExecutesTransform;
import net.strokkur.commands.internal.parsing.ExecutesTransform;
import net.strokkur.commands.internal.printer.CommonCommandTreePrinter;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.internal.util.MessagerWrapper;
import org.jspecify.annotations.NullUnmarked;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.Set;

public abstract class StrokkCommandsProcessor<C extends CommandInformation> extends AbstractProcessor {

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(Command.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  protected abstract void init();

  protected abstract PlatformUtils getPlatformUtils();

  protected abstract CommonTreePostProcessor createPostProcessor(MessagerWrapper messager);

  protected abstract CommonCommandTreePrinter<C> createPrinter(CommandNode node, C commandInformation);

  protected abstract ExecutesTransform createExecutesTransform(CommandParser parser);

  protected abstract DefaultExecutesTransform createDefaultExecutesTransform(CommandParser parser);

  protected abstract C getCommandInformation(SourceClass sourceClass);

  @Override
  @NullUnmarked
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    init();

    final MessagerWrapper messagerWrapper = MessagerWrapper.wrap(super.processingEnv.getMessager());
    final PlatformUtils platformUtils = getPlatformUtils();
    final CommandParser parser = new CommandParserImpl(messagerWrapper, platformUtils, this::createExecutesTransform, this::createDefaultExecutesTransform);
    final CommonTreePostProcessor treePostProcessor = createPostProcessor(messagerWrapper);

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

      final SourceClass sourceClass = typeElement.getKind() == ElementKind.RECORD
          ? new SourceRecordImpl(this.processingEnv, (DeclaredType) typeElement.asType())
          : new SourceClassImpl(this.processingEnv, (DeclaredType) typeElement.asType());

      try {
        processElement(sourceClass, messagerWrapper, parser, treePostProcessor);
      } catch (Exception e) {
        messagerWrapper.errorElement("An error occurred: {}", typeElement, e.getMessage());
        e.printStackTrace();
      }

      if (debugOnly != null) {
        break;
      }
    }

    return true;
  }

  private void processElement(
      final SourceClass sourceClass,
      final MessagerWrapper messagerWrapper,
      final CommandParser parser,
      final CommonTreePostProcessor treePostProcessor
  ) {
    boolean debug = System.getProperty(MessagerWrapper.DEBUG_SYSTEM_PROPERTY) != null;

    final C commandInformation = getCommandInformation(sourceClass);
    final CommandNode commandTree = parser.createCommandTree(sourceClass.getAnnotationElseThrow(Command.class).value(), sourceClass);
    if (commandTree == null) {
      return;
    }

    // Before we print the paths we do some post-processing to move some stuff around, which
    // is relevant for certain things to print correctly (a.e. executor requirements).
    treePostProcessor.cleanupPath(commandTree);
    treePostProcessor.applyDefaultExecutorPaths(commandTree);

    if (debug) {
      // debug log all paths.
      messagerWrapper.debug("Command Tree: \n\n{}\n ", commandTree.toString());
    }

    try {
      final CommonCommandTreePrinter<C> printer = createPrinter(commandTree, commandInformation);
      final JavaFileObject obj = processingEnv.getFiler().createSourceFile(printer.getPackageName() + "." + printer.getBrigadierClassName());

      try (PrintWriter out = new PrintWriter(obj.openWriter())) {
        printer.setWriter(out);
        printer.print();
      }
    } catch (Exception ex) {
      messagerWrapper.errorSource("A fatal exception occurred whilst printing source file: {}", sourceClass, ex.getMessage());
      ex.printStackTrace();
    }
  }
}
