package net.strokkur.commands.internal.util;

import net.strokkur.commands.annotations.Permission;
import net.strokkur.commands.annotations.RequiresOP;
import net.strokkur.commands.internal.intermediate.Requirement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Utils {

    @Nullable
    static TypeMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
        return getAnnotationMirror(element, annotationClass, "value");
    }

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

    @NullUnmarked
    static @NonNull List<Requirement> getAnnotatedRequirements(@NonNull Element element) {
        Permission permission = element.getAnnotation(Permission.class);
        RequiresOP requiresOP = element.getAnnotation(RequiresOP.class);

        List<Requirement> requirements = new ArrayList<>(2);
        if (permission != null) {
            requirements.add(Requirement.ofPermission(permission.value()));
        }
        if (requiresOP != null) {
            requirements.add(Requirement.IS_OP);
        }
        return requirements;
    }
}