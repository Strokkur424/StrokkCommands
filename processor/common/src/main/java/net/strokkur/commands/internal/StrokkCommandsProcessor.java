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

import net.strokkur.commands.CustomExecutorWrapper;
import net.strokkur.commands.CustomRequirement;
import net.strokkur.commands.CustomSuggestion;
import net.strokkur.commands.internal.exceptions.ProviderAlreadyRegisteredException;
import net.strokkur.commands.internal.intermediate.TreePostProcessor;
import net.strokkur.commands.internal.intermediate.registrable.ExecutorWrapperRegistry;
import net.strokkur.commands.internal.intermediate.registrable.RegistrableRegistry;
import net.strokkur.commands.internal.intermediate.registrable.RequirementRegistry;
import net.strokkur.commands.internal.intermediate.registrable.SuggestionsRegistry;
import net.strokkur.commands.internal.intermediate.tree.CommandNode;
import net.strokkur.commands.internal.parsing.CommandParsingSourceVisitor;
import net.strokkur.commands.internal.printer.CommonClassBuilder;
import net.strokkur.commands.internal.util.CommandInformation;
import net.strokkur.commands.meta.StrokkCommandsDebug;
import net.strokkur.jap.code.CodeGenUtil;
import net.strokkur.jap.code.classmodel.CodeClass;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.source.SourceMapProcessor;
import net.strokkur.jap.source.SourceMapUtil;
import net.strokkur.jap.source.annotation.SourceAnnotation;
import net.strokkur.jap.source.classmodel.SourceAnnotationInterface;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceRecord;
import net.strokkur.jap.source.util.MessagerWrapper;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class StrokkCommandsProcessor<A extends Annotation, C extends CommandInformation>
  extends AbstractProcessor
  implements SourceMapProcessor {

  private static @Nullable MessagerWrapper MESSAGER = null;

  public static MessagerWrapper messagerWrapper() {
    return Objects.requireNonNull(MESSAGER);
  }

  protected abstract Class<A> targetAnnotationClass();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(targetAnnotationClass().getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  protected abstract CommonClassBuilder<C> createBuilder(CommandNode node, C commandInformation);

  protected abstract C getCommandInformation(SourceClassLike sourceClass);

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    MESSAGER = messager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    final SourceMapUtil sourceMap = new SourceMapUtil(this);

    fillRegistry(CustomSuggestion.class, SuggestionsRegistry.get(), roundEnv);
    fillRegistry(CustomRequirement.class, RequirementRegistry.get(), roundEnv);
    fillRegistry(CustomExecutorWrapper.class, ExecutorWrapperRegistry.get(), roundEnv);

    CodeClassType debugOnly = null;

    final Optional<? extends Element> debugAnnotation = roundEnv.getElementsAnnotatedWith(StrokkCommandsDebug.class).stream().findFirst();
    if (debugAnnotation.isPresent()) {
      System.setProperty(MessagerWrapper.DEBUG_SYSTEM_PROPERTY, "true");

      final SourceClassLike annotated = sourceMap.parseClassElement((TypeElement) debugAnnotation.get());
      if (annotated.hasAnnotation(StrokkCommandsDebug.class)) {
        final SourceAnnotation debugAnnotationType = annotated.firstAnnotationByType(StrokkCommandsDebug.class);

        if (debugAnnotationType.isSet("only")) {
          debugOnly = debugAnnotationType.parameter("only").classValue();
        }
      }
    } else {
      System.clearProperty(MessagerWrapper.DEBUG_SYSTEM_PROPERTY);
    }

    for (Element element : roundEnv.getElementsAnnotatedWith(targetAnnotationClass())) {
      if (!(element instanceof TypeElement typeElement)) {
        // Element is not a class.
        continue;
      }

      final SourceClassLike sourceClass = sourceMap.parseClassElement(typeElement);
      if (sourceClass.enclosingClass() != null) {
        messager().warnSource(
          "This class is annotated with @%s, but is nested. This is unsupported behavior. If this " +
            "class is meant as a subcommand, annotate it with @Subcommand instead",
          sourceClass,
          targetAnnotationClass().getSimpleName()
        );
        continue;
      }

      if (debugOnly != null && !sourceClass.toClassType().equals(debugOnly)) {
        continue;
      }

      if (!(sourceClass instanceof SourceClass) && !(sourceClass instanceof SourceRecord)) {
        messager().warnSource(
          "Command classes should be non-abstract classes or records.",
          sourceClass
        );
        continue;
      }

      try {
        processElement(sourceClass);
      } catch (Exception e) {
        messager().errorSource("An error occurred: {}", sourceClass, e.getMessage());
        e.printStackTrace();
      }

      if (debugOnly != null) {
        break;
      }
    }

    return true;
  }

  private void processElement(SourceClassLike sourceClass) {
    final boolean debug = System.getProperty(MessagerWrapper.DEBUG_SYSTEM_PROPERTY) != null;

    final C commandInformation = getCommandInformation(sourceClass);
    final CommandNode commandTree = CommandParsingSourceVisitor.get().visitCommandClass(sourceClass, null);

    if (debug) {
      // debug log all paths.
      messager().debug("\nCommand Tree (Before PostProcess):\n{}\n ", commandTree.toString());
    }

    // Before we print the paths, we do some post-processing to move some stuff around, which
    // is relevant for certain things to print correctly (a.e. executor requirements).
    TreePostProcessor.get().cleanupPath(commandTree);
    TreePostProcessor.get().applyDefaultExecutorPaths(commandTree);

    if (debug) {
      // debug log all paths.
      messager().debug("\nCommand Tree:\n{}\n ", commandTree.toString());
    }

    final CodeGenUtil codeGen = new CodeGenUtil(this);
    try {
      final CommonClassBuilder<C> builder = createBuilder(commandTree, commandInformation);
      final CodeClass theClass = builder.createClass();
      codeGen.printJavaFile(theClass);
      messager().info("Printed: " + theClass.classType().fullyQualifiedName());
    } catch (Exception ex) {
      messager().errorSource("A fatal exception occurred whilst printing source file: {}", sourceClass, ex.getMessage());
      ex.printStackTrace();
    }
  }

  private void fillRegistry(
    Class<? extends Annotation> annotationClass,
    RegistrableRegistry<?> registry,
    RoundEnvironment roundEnv
  ) {
    final SourceMapUtil sourceMap = new SourceMapUtil(this);
    for (Element element : roundEnv.getElementsAnnotatedWith(annotationClass)) {
      try {
        if (element.getKind() != ElementKind.ANNOTATION_TYPE || !(element instanceof TypeElement typeElement)) {
          processingEnv.getMessager().printError(
            "non-annotation type annotated with @" + annotationClass.getSimpleName(),
            element
          );
          continue;
        }

        final SourceAnnotationInterface annotationInterface = (SourceAnnotationInterface) sourceMap.parseClassElement(typeElement);
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(typeElement)) {
          if (registry.tryRegisterProvider(
            annotationInterface,
            sourceMap.parseElement(annotatedElement)
          )) {
            break;
          }
        }
      } catch (ProviderAlreadyRegisteredException suggestion) {
        processingEnv.getMessager().printError(
          suggestion.getMessage(),
          element
        );
      }
    }
  }

  @Override
  public ProcessingEnvironment processingEnv() {
    return processingEnv;
  }
}
