package net.strokkur.commands.internal;

import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Optional;

public interface Utils {

    @Nullable
    static TypeMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass, String fieldName) {
        String annotationName = annotationClass.getName();

        Optional<? extends AnnotationMirror> out = element.getAnnotationMirrors().stream()
            .filter(mirror -> ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationName))
            .findFirst();

        return out.flatMap(mirror -> mirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().contentEquals(fieldName))
                .map(entry -> (TypeMirror) entry.getValue().getValue())
                .findFirst())
            .orElse(null);
    }
}