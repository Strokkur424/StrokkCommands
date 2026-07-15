package net.strokkur.internal.paper.ap;

import net.strokkur.jap.source.SourceMapProcessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedOptions("argumentDataPath")
public class ArgumentConverterProcessor extends AbstractProcessor implements SourceMapProcessor {
  private boolean hasRun = false;

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (hasRun) {
      return false;
    }

    final String dataPath = processingEnv.getOptions().get("argumentDataPath");
    // Parse it!

    hasRun = true;
    return false;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of("*");
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public ProcessingEnvironment processingEnv() {
    return processingEnv;
  }
}
