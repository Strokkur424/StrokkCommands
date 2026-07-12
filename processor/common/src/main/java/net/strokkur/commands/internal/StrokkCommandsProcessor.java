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
import net.strokkur.commands.internal.arguments.BrigadierArgumentConverter;
import net.strokkur.commands.internal.exceptions.ProviderAlreadyRegisteredException;
import net.strokkur.commands.internal.intermediate.CommonTreePostProcessor;
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
import net.strokkur.jap.code.convert.ConvertToClassType;
import net.strokkur.jap.code.documentation.AbstractDocumentationRenderer;
import net.strokkur.jap.code.documentation.MarkdownJavadocRenderer;
import net.strokkur.jap.code.documentation.StarJavadocRenderer;
import net.strokkur.jap.code.type.CodeClassType;
import net.strokkur.jap.code.type.CodePackage;
import net.strokkur.jap.source.SourceMapProcessor;
import net.strokkur.jap.source.SourceMapUtil;
import net.strokkur.jap.source.annotation.SourceAnnotation;
import net.strokkur.jap.source.classmodel.SourceAnnotationInterface;
import net.strokkur.jap.source.classmodel.SourceClass;
import net.strokkur.jap.source.classmodel.SourceClassLike;
import net.strokkur.jap.source.classmodel.SourceRecord;
import net.strokkur.jap.source.util.MessagerWrapper;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static net.strokkur.jap.code.documentation.AbstractDocumentationRenderer.createContext;

public abstract class StrokkCommandsProcessor<A extends Annotation, C extends CommandInformation>
  extends AbstractProcessor
  implements SourceMapProcessor {

  protected abstract Class<A> targetAnnotationClass();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(targetAnnotationClass().getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  protected void init() {
    // noop
  }

  protected abstract PlatformUtils getPlatformUtils();

  protected abstract CommonTreePostProcessor createPostProcessor(MessagerWrapper messager);

  protected abstract CommonClassBuilder<C> createBuilder(CommandNode node, C commandInformation);

  protected abstract BrigadierArgumentConverter getConverter(MessagerWrapper messager);

  protected abstract C getCommandInformation(SourceClassLike sourceClass);

  protected AbstractDocumentationRenderer createDocumentationRenderer(CodePackage pkg, Set<? extends ConvertToClassType> imports) {
    final AbstractDocumentationRenderer.Context ctx = createContext(pkg, imports);
    if (isJava25()) {
      return new MarkdownJavadocRenderer(ctx);
    }
    // We are on Java 24 or below, so use the star Javadoc visitor.
    return new StarJavadocRenderer(ctx);
  }

  private boolean isJava25() {
    try {
      SourceVersion.valueOf("RELEASE_25");
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    init();
    final SourceMapUtil sourceMap = new SourceMapUtil(this);

    final MessagerWrapper messagerWrapper = MessagerWrapper.wrap(super.processingEnv.getMessager());
    final SuggestionsRegistry suggestionsRegistry = createAndFillRegistry(CustomSuggestion.class, SuggestionsRegistry::new, roundEnv, messagerWrapper);
    final RequirementRegistry requirementRegistry = createAndFillRegistry(CustomRequirement.class, RequirementRegistry::new, roundEnv, messagerWrapper);
    final ExecutorWrapperRegistry executorWrapperRegistry = createAndFillRegistry(CustomExecutorWrapper.class, ExecutorWrapperRegistry::new, roundEnv, messagerWrapper);

    final NodeUtils nodeUtils = new NodeUtils(getPlatformUtils(), messagerWrapper, getConverter(messagerWrapper), suggestionsRegistry, requirementRegistry, executorWrapperRegistry);
    final CommandParsingSourceVisitor parser = new CommandParsingSourceVisitor(messagerWrapper, nodeUtils);
    final CommonTreePostProcessor treePostProcessor = createPostProcessor(messagerWrapper);

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
        messagerWrapper.warnSource(
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
        messagerWrapper.warnSource(
          "Command classes should be non-abstract classes or records.",
          sourceClass
        );
        continue;
      }

      try {
        processElement(sourceClass, messagerWrapper, parser, treePostProcessor);
      } catch (Exception e) {
        messagerWrapper.errorSource("An error occurred: {}", sourceClass, e.getMessage());
        e.printStackTrace();
      }

      if (debugOnly != null) {
        break;
      }
    }

    return true;
  }

  private void processElement(
    SourceClassLike sourceClass,
    MessagerWrapper messagerWrapper,
    CommandParsingSourceVisitor parser,
    CommonTreePostProcessor treePostProcessor
  ) {
    final boolean debug = System.getProperty(MessagerWrapper.DEBUG_SYSTEM_PROPERTY) != null;

    final C commandInformation = getCommandInformation(sourceClass);
    final CommandNode commandTree = parser.visitCommandClass(sourceClass, null);

    if (debug) {
      // debug log all paths.
      messagerWrapper.debug("\nCommand Tree (Before PostProcess):\n{}\n ", commandTree.toString());
    }

    // Before we print the paths, we do some post-processing to move some stuff around, which
    // is relevant for certain things to print correctly (a.e. executor requirements).
    treePostProcessor.cleanupPath(commandTree);
    treePostProcessor.applyDefaultExecutorPaths(commandTree);

    if (debug) {
      // debug log all paths.
      messagerWrapper.debug("\nCommand Tree:\n{}\n ", commandTree.toString());
    }

    final CodeGenUtil codeGen = new CodeGenUtil(this);
    try {
      final CommonClassBuilder<C> builder = createBuilder(commandTree, commandInformation);
      codeGen.printJavaFile(builder.createClass());
    } catch (Exception ex) {
      messagerWrapper.errorSource("A fatal exception occurred whilst printing source file: {}", sourceClass, ex.getMessage());
      ex.printStackTrace();
    }
  }

  private <T extends RegistrableRegistry<?>> T createAndFillRegistry(
    Class<? extends Annotation> annotationClass,
    Function<CodeClassType, T> ctor,
    RoundEnvironment roundEnv,
    MessagerWrapper messager
  ) {
    final SourceMapUtil sourceMap = new SourceMapUtil(this);
    final T registry = ctor.apply(getPlatformUtils().platformType());

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
            messager,
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
    return registry;
  }
}
